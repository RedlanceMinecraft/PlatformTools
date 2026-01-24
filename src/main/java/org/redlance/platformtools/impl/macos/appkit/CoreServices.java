package org.redlance.platformtools.impl.macos.appkit;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

public interface CoreServices extends Library {
    CoreServices INSTANCE = Native.load("CoreServices", CoreServices.class);

    int kCFURLPOSIXPathStyle = 0;

    Pointer MDItemCreate(Pointer allocator, Pointer path);
    Pointer MDItemCopyAttribute(Pointer item, Pointer name);

    Pointer CFURLCreateWithFileSystemPath(Pointer allocator, Pointer filePath, int pathStyle, boolean isDirectory);
    Pointer CFURLCopyFileSystemPath(Pointer anURL, int pathStyle);

    Pointer LSSharedFileListCreate(Pointer allocator, Pointer listType, Pointer listOptions);
    Pointer LSSharedFileListCopySnapshot(Pointer list, Pointer seed);

    int LSSharedFileListItemRemove(Pointer list, Pointer item);
    int LSSharedFileListItemResolve(Pointer item, int flags, PointerByReference outURL, Pointer outRef);
    Pointer LSSharedFileListInsertItemURL(Pointer list, Pointer insertAfter, Pointer displayName, Pointer iconRef, Pointer fileURL, Pointer propertiesToSet, Pointer propertiesToClear);

    void CFRelease(Pointer cfTypeRef);
    void CFRetain(Pointer cfTypeRef);
}
