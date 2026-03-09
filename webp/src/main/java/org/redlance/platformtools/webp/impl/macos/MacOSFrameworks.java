package org.redlance.platformtools.webp.impl.macos;

import org.jetbrains.annotations.Nullable;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;

public final class MacOSFrameworks {
    static final int kCFStringEncodingUTF8 = 0x08000100;
    static final int kCGImageAlphaLast = 3;
    static final int kCGImageAlphaPremultipliedLast = 1;
    static final int kCFNumberFloat64Type = 13;

    final SymbolLookup lookup;
    final SymbolLookup imageIO;

    final MethodHandle cfRelease;
    final MethodHandle cgColorSpaceCreateDeviceRGB;
    final MethodHandle cgColorSpaceRelease;
    final MethodHandle cgImageRelease;
    private final MethodHandle cfStringCreateWithCString;

    final MethodHandle cfDataCreateMutable;
    final MethodHandle cgImageDestCreateWithData;

    private MacOSFrameworks(SymbolLookup combined, SymbolLookup imageIO) {
        this.lookup = combined;
        this.imageIO = imageIO;

        Linker linker = Linker.nativeLinker();

        this.cfRelease = linker.downcallHandle(
                combined.find("CFRelease").orElseThrow(),
                FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
        );
        this.cfStringCreateWithCString = linker.downcallHandle(
                combined.find("CFStringCreateWithCString").orElseThrow(),
                FunctionDescriptor.of(
                        ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT
                )
        );
        this.cgColorSpaceCreateDeviceRGB = linker.downcallHandle(
                combined.find("CGColorSpaceCreateDeviceRGB").orElseThrow(),
                FunctionDescriptor.of(ValueLayout.ADDRESS)
        );
        this.cgColorSpaceRelease = linker.downcallHandle(
                combined.find("CGColorSpaceRelease").orElseThrow(),
                FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
        );
        this.cgImageRelease = linker.downcallHandle(
                combined.find("CGImageRelease").orElseThrow(),
                FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
        );

        this.cfDataCreateMutable = linker.downcallHandle(
                combined.find("CFDataCreateMutable").orElseThrow(),
                FunctionDescriptor.of(
                        ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG
                )
        );
        this.cgImageDestCreateWithData = linker.downcallHandle(
                imageIO.find("CGImageDestinationCreateWithData").orElseThrow(),
                FunctionDescriptor.of(
                        ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS
                )
        );
    }

    private static class Holder {
        static final @Nullable MacOSFrameworks INSTANCE = tryCreate();
    }

    public static @Nullable MacOSFrameworks getInstance() {
        return Holder.INSTANCE;
    }

    private static @Nullable MacOSFrameworks tryCreate() {
        try {
            return create();
        } catch (Throwable t) {
            return null;
        }
    }

    public static MacOSFrameworks create() {
        SymbolLookup defaultLookup = Linker.nativeLinker().defaultLookup();
        SymbolLookup io = SymbolLookup.libraryLookup(
                "/System/Library/Frameworks/ImageIO.framework/ImageIO", Arena.global()
        );
        SymbolLookup combined = name -> io.find(name).or(() -> defaultLookup.find(name));
        return new MacOSFrameworks(combined, io);
    }

    MemorySegment createCFString(Arena arena, String s) throws Throwable {
        return (MemorySegment) cfStringCreateWithCString.invokeExact(
                MemorySegment.NULL, arena.allocateFrom(s), kCFStringEncodingUTF8
        );
    }

    static void unpremultiplyAlpha(byte[] rgba) {
        for (int i = 0; i < rgba.length; i += 4) {
            int a = rgba[i + 3] & 0xFF;
            if (a == 0) {
                rgba[i] = rgba[i + 1] = rgba[i + 2] = 0;
            } else if (a < 255) {
                rgba[i]     = (byte) Math.min(255, ((rgba[i] & 0xFF) * 255 + a / 2) / a);
                rgba[i + 1] = (byte) Math.min(255, ((rgba[i + 1] & 0xFF) * 255 + a / 2) / a);
                rgba[i + 2] = (byte) Math.min(255, ((rgba[i + 2] & 0xFF) * 255 + a / 2) / a);
            }
        }
    }
}
