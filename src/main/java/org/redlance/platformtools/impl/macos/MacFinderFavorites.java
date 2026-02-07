package org.redlance.platformtools.impl.macos;

import ca.weblite.objc.RuntimeUtils;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;
import org.jetbrains.annotations.Nullable;
import org.redlance.platformtools.PlatformFinderFavorites;
import org.redlance.platformtools.impl.macos.appkit.CoreFoundation;
import org.redlance.platformtools.impl.macos.appkit.CoreServices;

public final class MacFinderFavorites implements PlatformFinderFavorites {

    private static @Nullable Pointer createSharedListFavoriteItems() {
        Pointer kLSSharedFileListFavoriteItems = NativeLibrary.getInstance("CoreServices")
                .getGlobalVariableAddress("kLSSharedFileListFavoriteItems")
                .getPointer(0);

        return CoreServices.INSTANCE.LSSharedFileListCreate(null, kLSSharedFileListFavoriteItems, null);
    }

    @Override
    public boolean isPinned(String path) {
        Pointer sharedList = createSharedListFavoriteItems();
        if (sharedList == null) return false;

        try {
            return isPinned(sharedList, path);
        } finally {
            CoreServices.INSTANCE.CFRelease(sharedList);
        }
    }

    private boolean isPinned(Pointer sharedList, String path) {
        Pointer item = getItemFromList(sharedList, path);
        if (item != null) CoreServices.INSTANCE.CFRelease(item);
        return item != null;
    }

    @Override
    public boolean pin(String path, boolean isFolder, Position position) {
        Pointer sharedList = createSharedListFavoriteItems();
        if (sharedList == null) return false;

        try {
            if (isPinned(sharedList, path)) return false;

            Pointer kLSSharedFileListItemLast = NativeLibrary.getInstance("CoreServices")
                    .getGlobalVariableAddress(position.macOS)
                    .getPointer(0);

            Pointer cfUrl = CoreServices.INSTANCE.CFURLCreateWithFileSystemPath(
                    null,
                    RuntimeUtils.str(path),
                    CoreServices.kCFURLPOSIXPathStyle,
                    isFolder
            );

            Pointer newItem = CoreServices.INSTANCE.LSSharedFileListInsertItemURL(
                    sharedList,
                    kLSSharedFileListItemLast,
                    null, // Ignored
                    null, // (Ignored?) TODO Icon
                    cfUrl,
                    null,
                    null
            );

            if (newItem != null) CoreServices.INSTANCE.CFRelease(newItem);
            CoreServices.INSTANCE.CFRelease(cfUrl);
            return newItem != null;
        } finally {
            CoreServices.INSTANCE.CFRelease(sharedList);
        }
    }

    @Override
    public boolean unpin(String path) {
        Pointer sharedList = createSharedListFavoriteItems();
        if (sharedList == null) return false;

        try {
            Pointer itemToRemove = getItemFromList(sharedList, path);
            if (itemToRemove == null) return false;

            int status = CoreServices.INSTANCE.LSSharedFileListItemRemove(
                    sharedList, itemToRemove
            );

            CoreServices.INSTANCE.CFRelease(itemToRemove);
            return status == 0;
        } finally {
            CoreServices.INSTANCE.CFRelease(sharedList);
        }
    }

    @Nullable
    private static Pointer getItemFromList(Pointer sharedList, String path) {
        Pointer snapshot = CoreServices.INSTANCE.LSSharedFileListCopySnapshot(sharedList, null);
        if (snapshot == null) return null;

        try {
            long count = CoreFoundation.INSTANCE.CFArrayGetCount(snapshot);

            for (long i = 0; i < count; i++) {
                Pointer itemPtr = CoreFoundation.INSTANCE.CFArrayGetValueAtIndex(snapshot, i);
                if (itemPtr == null) continue;

                PointerByReference urlRef = new PointerByReference();

                // 4 = kLSSharedFileListDoNotMountVolumes
                int status = CoreServices.INSTANCE.LSSharedFileListItemResolve(itemPtr, 4, urlRef, null);

                if (status == 0 && urlRef.getValue() != null) {
                    Pointer cfUrl = urlRef.getValue();
                    boolean match = false;

                    Pointer cfPathStr = CoreServices.INSTANCE.CFURLCopyFileSystemPath(cfUrl, CoreServices.kCFURLPOSIXPathStyle);
                    if (cfPathStr != null) {
                        String foundPath = RuntimeUtils.msgString(cfPathStr, "UTF8String");
                        if (path.equals(foundPath)) match = true;

                        CoreServices.INSTANCE.CFRelease(cfPathStr);
                    }
                    CoreServices.INSTANCE.CFRelease(cfUrl);

                    if (match) {
                        CoreServices.INSTANCE.CFRetain(itemPtr);
                        return itemPtr;
                    }
                }
            }
        } finally {
            CoreServices.INSTANCE.CFRelease(snapshot);
        }

        return null;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}
