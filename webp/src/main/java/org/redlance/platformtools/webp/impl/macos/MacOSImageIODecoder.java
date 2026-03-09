package org.redlance.platformtools.webp.impl.macos;

import org.jetbrains.annotations.Nullable;
import org.redlance.platformtools.webp.decoder.DecodedImage;
import org.redlance.platformtools.webp.decoder.PlatformWebPDecoder;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.nio.ByteOrder;

public final class MacOSImageIODecoder implements PlatformWebPDecoder {
    private final MacOSFrameworks fw;

    private final MethodHandle cfDataCreate;
    private final MethodHandle cgImageSourceCreateWithData;
    private final MethodHandle cgImageSourceCreateImageAtIndex;
    private final MethodHandle cgImageGetWidth;
    private final MethodHandle cgImageGetHeight;
    private final MethodHandle cgImageGetBitmapInfo;
    private final MethodHandle cgImageGetDataProvider;
    private final MethodHandle cgDataProviderCopyData;
    private final MethodHandle cfDataGetBytePtr;
    private final MethodHandle cfDataGetLength;

    private MacOSImageIODecoder(MacOSFrameworks fw) {
        this.fw = fw;

        Linker linker = Linker.nativeLinker();

        this.cfDataCreate = linker.downcallHandle(
                fw.lookup.find("CFDataCreate").orElseThrow(),
                FunctionDescriptor.of(
                        ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG
                )
        );
        this.cgImageSourceCreateWithData = linker.downcallHandle(
                fw.imageIO.find("CGImageSourceCreateWithData").orElseThrow(),
                FunctionDescriptor.of(
                        ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS
                )
        );
        this.cgImageSourceCreateImageAtIndex = linker.downcallHandle(
                fw.imageIO.find("CGImageSourceCreateImageAtIndex").orElseThrow(),
                FunctionDescriptor.of(
                        ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS
                )
        );
        this.cgImageGetWidth = linker.downcallHandle(
                fw.lookup.find("CGImageGetWidth").orElseThrow(),
                FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS)
        );
        this.cgImageGetHeight = linker.downcallHandle(
                fw.lookup.find("CGImageGetHeight").orElseThrow(),
                FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS)
        );
        this.cgImageGetBitmapInfo = linker.downcallHandle(
                fw.lookup.find("CGImageGetBitmapInfo").orElseThrow(),
                FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS)
        );
        this.cgImageGetDataProvider = linker.downcallHandle(
                fw.lookup.find("CGImageGetDataProvider").orElseThrow(),
                FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS)
        );
        this.cgDataProviderCopyData = linker.downcallHandle(
                fw.lookup.find("CGDataProviderCopyData").orElseThrow(),
                FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS)
        );
        this.cfDataGetBytePtr = linker.downcallHandle(
                fw.lookup.find("CFDataGetBytePtr").orElseThrow(),
                FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS)
        );
        this.cfDataGetLength = linker.downcallHandle(
                fw.lookup.find("CFDataGetLength").orElseThrow(),
                FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS)
        );
    }

    public static @Nullable MacOSImageIODecoder tryCreate() {
        MacOSFrameworks fw = MacOSFrameworks.getInstance();
        return fw != null ? new MacOSImageIODecoder(fw) : null;
    }

    public static MacOSImageIODecoder create() throws Throwable {
        return new MacOSImageIODecoder(MacOSFrameworks.create());
    }

    @Override
    public String backendName() {
        return "macOS ImageIO";
    }

    @Override
    public DecodedImage decode(byte[] webpData) {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment webpSeg = arena.allocate(webpData.length);
            webpSeg.copyFrom(MemorySegment.ofArray(webpData));

            MemorySegment cfData = (MemorySegment) this.cfDataCreate.invokeExact(
                    MemorySegment.NULL, webpSeg, (long) webpData.length
            );

            MemorySegment source = (MemorySegment) this.cgImageSourceCreateWithData.invokeExact(cfData, MemorySegment.NULL);
            if (source.address() == 0) {
                this.fw.cfRelease.invokeExact(cfData);
                throw new IllegalStateException("CGImageSourceCreateWithData failed");
            }

            MemorySegment cgImage = (MemorySegment) this.cgImageSourceCreateImageAtIndex.invokeExact(
                    source, 0L, MemorySegment.NULL
            );
            if (cgImage.address() == 0) {
                this.fw.cfRelease.invokeExact(source);
                this.fw.cfRelease.invokeExact(cfData);
                throw new IllegalStateException("CGImageSourceCreateImageAtIndex failed");
            }

            long w = (long) this.cgImageGetWidth.invokeExact(cgImage);
            long h = (long) this.cgImageGetHeight.invokeExact(cgImage);
            int bitmapInfo = (int) this.cgImageGetBitmapInfo.invokeExact(cgImage);

            // Read raw pixel data from CGImage
            MemorySegment provider = (MemorySegment) this.cgImageGetDataProvider.invokeExact(cgImage);
            MemorySegment rawData = (MemorySegment) this.cgDataProviderCopyData.invokeExact(provider);
            if (rawData.address() == 0) {
                this.fw.cgImageRelease.invokeExact(cgImage);
                this.fw.cfRelease.invokeExact(source);
                this.fw.cfRelease.invokeExact(cfData);
                throw new IllegalStateException("CGDataProviderCopyData failed");
            }

            long len = (long) this.cfDataGetLength.invokeExact(rawData);
            MemorySegment ptr = (MemorySegment) this.cfDataGetBytePtr.invokeExact(rawData);
            MemorySegment pixels = ptr.reinterpret(len);

            int[] argb = readPixels(pixels, (int) w, (int) h, bitmapInfo);

            this.fw.cfRelease.invokeExact(rawData);
            this.fw.cgImageRelease.invokeExact(cgImage);
            this.fw.cfRelease.invokeExact(source);
            this.fw.cfRelease.invokeExact(cfData);

            return new DecodedImage(argb, (int) w, (int) h);
        } catch (RuntimeException | Error e) {
            throw e;
        } catch (Throwable t) {
            throw new RuntimeException("macOS ImageIO decode failed", t);
        }
    }

    private static int[] readPixels(MemorySegment pixels, int w, int h, int bitmapInfo) {
        int alphaInfo = bitmapInfo & 0x1F; // kCGBitmapAlphaInfoMask
        int byteOrder = bitmapInfo & 0x7000; // kCGBitmapByteOrderMask
        boolean premultiplied = alphaInfo == MacOSFrameworks.kCGImageAlphaPremultipliedFirst
                || alphaInfo == MacOSFrameworks.kCGImageAlphaPremultipliedLast;

        int[] argb;
        if (byteOrder == MacOSFrameworks.kCGBitmapByteOrder32Little) {
            // Native LE int = BGRA bytes = ARGB int
            argb = pixels.toArray(ValueLayout.JAVA_INT);
        } else {
            // Big-endian / default: ARGB bytes → read as BE int
            argb = pixels.toArray(ValueLayout.JAVA_INT.withOrder(ByteOrder.BIG_ENDIAN));
        }

        if (premultiplied) {
            MacOSFrameworks.unpremultiplyAlpha(argb);
        }

        // Handle AlphaLast / SkipLast formats (RGBA/RGBX) → convert to ARGB
        if (alphaInfo == MacOSFrameworks.kCGImageAlphaPremultipliedLast
                || alphaInfo == MacOSFrameworks.kCGImageAlphaLast
                || alphaInfo == MacOSFrameworks.kCGImageAlphaNoneSkipLast) {
            for (int i = 0; i < argb.length; i++) {
                int px = argb[i];
                // RGBA → ARGB: rotate right by 8
                argb[i] = (px << 24) | (px >>> 8);
            }
        }

        return argb;
    }

    @Override
    public int[] getInfo(byte[] webpData) {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment webpSeg = arena.allocate(webpData.length);
            webpSeg.copyFrom(MemorySegment.ofArray(webpData));

            MemorySegment cfData = (MemorySegment) this.cfDataCreate.invokeExact(
                    MemorySegment.NULL, webpSeg, (long) webpData.length
            );

            MemorySegment source = (MemorySegment) this.cgImageSourceCreateWithData.invokeExact(
                    cfData, MemorySegment.NULL
            );
            if (source.address() == 0) {
                this.fw.cfRelease.invokeExact(cfData);
                throw new IllegalStateException("CGImageSourceCreateWithData failed");
            }

            MemorySegment cgImage = (MemorySegment) this.cgImageSourceCreateImageAtIndex.invokeExact(
                    source, 0L, MemorySegment.NULL
            );
            if (cgImage.address() == 0) {
                this.fw.cfRelease.invokeExact(source);
                this.fw.cfRelease.invokeExact(cfData);
                throw new IllegalStateException("CGImageSourceCreateImageAtIndex failed");
            }

            long w = (long) this.cgImageGetWidth.invokeExact(cgImage);
            long h = (long) this.cgImageGetHeight.invokeExact(cgImage);

            this.fw.cgImageRelease.invokeExact(cgImage);
            this.fw.cfRelease.invokeExact(source);
            this.fw.cfRelease.invokeExact(cfData);

            return new int[] {(int) w, (int) h};
        } catch (RuntimeException | Error e) {
            throw e;
        } catch (Throwable t) {
            throw new RuntimeException("macOS ImageIO getInfo failed", t);
        }
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}
