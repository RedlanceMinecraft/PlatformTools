package org.redlance.platformtools.webp.impl.libwebp;

import org.jetbrains.annotations.Nullable;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.nio.file.Path;

public final class LibWebPLibrary {
    final SymbolLookup lookup;
    final MethodHandle webPFree;

    private LibWebPLibrary(SymbolLookup lookup) {
        this.lookup = lookup;

        this.webPFree = Linker.nativeLinker().downcallHandle(
                lookup.find("WebPFree").orElseThrow(),
                FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
        );
    }

    private static class Holder {
        static final @Nullable LibWebPLibrary INSTANCE = tryLoad();
    }

    public static @Nullable LibWebPLibrary getInstance() {
        return Holder.INSTANCE;
    }

    private static @Nullable LibWebPLibrary tryLoad() {
        for (String name : new String[]{"webp", "libwebp", "libwebp.so.7"}) {
            try {
                return new LibWebPLibrary(SymbolLookup.libraryLookup(name, Arena.global()));
            } catch (Throwable ignored) {
            }
        }

        String os = System.getProperty("os.name", "").toLowerCase();
        String[] paths;

        if (os.contains("mac")) {
            paths = new String[]{
                    "/opt/homebrew/lib/libwebp.dylib",
                    "/usr/local/lib/libwebp.dylib",
                    "/opt/homebrew/lib/libwebp.7.dylib",
                    "/usr/local/lib/libwebp.7.dylib"
            };
        } else if (os.contains("win")) {
            String programFiles = System.getenv("ProgramFiles");
            String localAppData = System.getenv("LOCALAPPDATA");
            paths = new String[]{
                    "libwebp.dll",
                    programFiles + "\\libwebp\\bin\\libwebp.dll",
                    localAppData + "\\libwebp\\bin\\libwebp.dll"
            };
        } else {
            paths = new String[]{
                    "/usr/lib/libwebp.so",
                    "/usr/lib/x86_64-linux-gnu/libwebp.so",
                    "/usr/lib/aarch64-linux-gnu/libwebp.so",
                    "/usr/local/lib/libwebp.so"
            };
        }

        for (String path : paths) {
            try {
                return new LibWebPLibrary(SymbolLookup.libraryLookup(Path.of(path), Arena.global()));
            } catch (Throwable ignored) {
            }
        }

        return null;
    }
}
