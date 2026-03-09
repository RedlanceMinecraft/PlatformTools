package org.redlance.platformtools.webp.impl.macos;

import org.jetbrains.annotations.Nullable;
import org.redlance.platformtools.webp.encoder.PlatformWebPEncoder;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;

public final class MacOSImageIOEncoder implements PlatformWebPEncoder {
    private final MacOSFrameworks fw;

    private final MethodHandle cfDataGetLength;
    private final MethodHandle cfDataGetBytePtr;
    private final MethodHandle cfNumberCreate;
    private final MethodHandle cfDictionaryCreate;
    private final MethodHandle cgImageCreate;
    private final MethodHandle cgDataProviderCreateWithData;
    private final MethodHandle cgDataProviderRelease;
    private final MethodHandle cgImageDestAddImage;
    private final MethodHandle cgImageDestFinalize;
    private final MemorySegment qualityPropertyKey;

    private MacOSImageIOEncoder(MacOSFrameworks fw) {
        this.fw = fw;

        Linker linker = Linker.nativeLinker();

        this.cfDataGetLength = linker.downcallHandle(
                fw.lookup.find("CFDataGetLength").orElseThrow(),
                FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS)
        );
        this.cfDataGetBytePtr = linker.downcallHandle(
                fw.lookup.find("CFDataGetBytePtr").orElseThrow(),
                FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS)
        );
        this.cfNumberCreate = linker.downcallHandle(
                fw.lookup.find("CFNumberCreate").orElseThrow(),
                FunctionDescriptor.of(
                        ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS
                )
        );
        this.cfDictionaryCreate = linker.downcallHandle(
                fw.lookup.find("CFDictionaryCreate").orElseThrow(),
                FunctionDescriptor.of(
                        ValueLayout.ADDRESS,
                        ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG,
                        ValueLayout.ADDRESS, ValueLayout.ADDRESS
                )
        );
        this.cgImageCreate = linker.downcallHandle(
                fw.lookup.find("CGImageCreate").orElseThrow(),
                FunctionDescriptor.of(
                        ValueLayout.ADDRESS,
                        ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG,
                        ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG,
                        ValueLayout.ADDRESS, ValueLayout.JAVA_INT,
                        ValueLayout.ADDRESS, ValueLayout.ADDRESS,
                        ValueLayout.JAVA_BOOLEAN, ValueLayout.JAVA_INT
                )
        );
        this.cgDataProviderCreateWithData = linker.downcallHandle(
                fw.lookup.find("CGDataProviderCreateWithData").orElseThrow(),
                FunctionDescriptor.of(
                        ValueLayout.ADDRESS,
                        ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS
                )
        );
        this.cgDataProviderRelease = linker.downcallHandle(
                fw.lookup.find("CGDataProviderRelease").orElseThrow(),
                FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
        );
        this.cgImageDestAddImage = linker.downcallHandle(
                fw.imageIO.find("CGImageDestinationAddImage").orElseThrow(),
                FunctionDescriptor.ofVoid(
                        ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS
                )
        );
        this.cgImageDestFinalize = linker.downcallHandle(
                fw.imageIO.find("CGImageDestinationFinalize").orElseThrow(),
                FunctionDescriptor.of(ValueLayout.JAVA_BOOLEAN, ValueLayout.ADDRESS)
        );
        this.qualityPropertyKey = fw.imageIO.find("kCGImageDestinationLossyCompressionQuality").orElseThrow()
                .reinterpret(ValueLayout.ADDRESS.byteSize())
                .get(ValueLayout.ADDRESS, 0);
    }

    public static @Nullable MacOSImageIOEncoder tryCreate() {
        MacOSFrameworks fw = MacOSFrameworks.getInstance();
        if (fw == null || !testCanEncode(fw)) return null;
        return new MacOSImageIOEncoder(fw);
    }

    private static boolean testCanEncode(MacOSFrameworks fw) {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment uti = fw.createCFString(arena, "org.webmproject.webp");
            if (uti.address() == 0) return false;

            MemorySegment data = (MemorySegment) fw.cfDataCreateMutable.invokeExact(MemorySegment.NULL, 0L);
            MemorySegment dest = (MemorySegment) fw.cgImageDestCreateWithData.invokeExact(
                    data, uti, 1L, MemorySegment.NULL
            );
            boolean result = dest.address() != 0;
            if (result) fw.cfRelease.invokeExact(dest);
            fw.cfRelease.invokeExact(data);
            fw.cfRelease.invokeExact(uti);
            return result;
        } catch (Throwable t) {
            return false;
        }
    }

    @Override
    public String backendName() {
        return "macOS ImageIO";
    }

    @Override
    public byte[] encodeLossless(int[] argb, int width, int height) {
        // ImageIO doesn't support true lossless WebP; use max quality lossy
        return encodeLossy(argb, width, height, 1.0f);
    }

    @Override
    public byte[] encodeLossy(int[] argb, int width, int height, float quality) {
        try (Arena arena = Arena.ofConfined()) {
            // CGDataProviderCreateWithData stores the pointer — must use arena-allocated memory
            int bufSize = argb.length * 4;
            MemorySegment argbSeg = arena.allocate(bufSize);
            argbSeg.copyFrom(MemorySegment.ofArray(argb));

            MemorySegment colorSpace = (MemorySegment) this.fw.cgColorSpaceCreateDeviceRGB.invokeExact();
            MemorySegment provider = (MemorySegment) this.cgDataProviderCreateWithData.invokeExact(
                    MemorySegment.NULL, argbSeg, (long) bufSize, MemorySegment.NULL
            );

            MemorySegment cgImage = (MemorySegment) this.cgImageCreate.invokeExact(
                    (long) width, (long) height,
                    8L, 32L, (long) width * 4,
                    colorSpace, MacOSFrameworks.kCGBitmapByteOrder32Little | MacOSFrameworks.kCGImageAlphaFirst,
                    provider, MemorySegment.NULL,
                    false, 0
            );

            if (cgImage.address() == 0) {
                this.cgDataProviderRelease.invokeExact(provider);
                this.fw.cgColorSpaceRelease.invokeExact(colorSpace);
                throw new IllegalStateException("CGImageCreate failed");
            }

            MemorySegment outputData = (MemorySegment) this.fw.cfDataCreateMutable.invokeExact(
                    MemorySegment.NULL, 0L
            );

            MemorySegment uti = this.fw.createCFString(arena, "org.webmproject.webp");
            MemorySegment dest = (MemorySegment) this.fw.cgImageDestCreateWithData.invokeExact(
                    outputData, uti, 1L, MemorySegment.NULL
            );

            if (dest.address() == 0) {
                this.fw.cfRelease.invokeExact(uti);
                this.fw.cfRelease.invokeExact(outputData);
                this.fw.cgImageRelease.invokeExact(cgImage);
                this.cgDataProviderRelease.invokeExact(provider);
                this.fw.cgColorSpaceRelease.invokeExact(colorSpace);
                throw new IllegalStateException("CGImageDestinationCreateWithData failed");
            }

            MemorySegment props = createQualityProperties(arena, quality);
            this.cgImageDestAddImage.invokeExact(dest, cgImage, props);
            boolean ok = (boolean) this.cgImageDestFinalize.invokeExact(dest);

            byte[] result = null;
            if (ok) {
                long len = (long) this.cfDataGetLength.invokeExact(outputData);
                MemorySegment ptr = (MemorySegment) this.cfDataGetBytePtr.invokeExact(outputData);
                result = ptr.reinterpret(len).toArray(ValueLayout.JAVA_BYTE);
            }

            this.fw.cfRelease.invokeExact(props);
            this.fw.cfRelease.invokeExact(dest);
            this.fw.cfRelease.invokeExact(uti);
            this.fw.cfRelease.invokeExact(outputData);
            this.fw.cgImageRelease.invokeExact(cgImage);
            this.cgDataProviderRelease.invokeExact(provider);
            this.fw.cgColorSpaceRelease.invokeExact(colorSpace);

            if (result == null) throw new IllegalStateException("CGImageDestinationFinalize failed");
            return result;
        } catch (RuntimeException | Error e) {
            throw e;
        } catch (Throwable t) {
            throw new RuntimeException("macOS ImageIO encode failed", t);
        }
    }

    private MemorySegment createQualityProperties(Arena arena, float quality) throws Throwable {
        MemorySegment doubleVal = arena.allocate(ValueLayout.JAVA_DOUBLE);
        doubleVal.set(ValueLayout.JAVA_DOUBLE, 0, quality);
        MemorySegment qualityNum = (MemorySegment) this.cfNumberCreate.invokeExact(
                MemorySegment.NULL, MacOSFrameworks.kCFNumberFloat64Type, doubleVal
        );

        MemorySegment keys = arena.allocate(ValueLayout.ADDRESS);
        keys.set(ValueLayout.ADDRESS, 0, this.qualityPropertyKey);

        MemorySegment values = arena.allocate(ValueLayout.ADDRESS);
        values.set(ValueLayout.ADDRESS, 0, qualityNum);

        MemorySegment dict = (MemorySegment) this.cfDictionaryCreate.invokeExact(
                MemorySegment.NULL, keys, values, 1L,
                MemorySegment.NULL, MemorySegment.NULL
        );

        this.fw.cfRelease.invokeExact(qualityNum);
        return dict;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}
