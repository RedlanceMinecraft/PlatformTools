package org.redlance.platformtools.webp.impl.macos;

import org.jetbrains.annotations.Nullable;
import org.redlance.platformtools.webp.decoder.PlatformWebPDecoder;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;

public final class MacOSImageIODecoder implements PlatformWebPDecoder {
    private final MacOSFrameworks fw;

    private final MethodHandle cfDataCreate;
    private final MethodHandle cgImageSourceCreateWithData;
    private final MethodHandle cgImageSourceCreateImageAtIndex;
    private final MethodHandle cgImageGetWidth;
    private final MethodHandle cgImageGetHeight;
    private final MethodHandle cgBitmapContextCreate;
    private final MethodHandle cgBitmapContextGetData;
    private final MethodHandle cgContextDrawImage;
    private final MethodHandle cgContextRelease;

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
        this.cgBitmapContextCreate = linker.downcallHandle(
                fw.lookup.find("CGBitmapContextCreate").orElseThrow(),
                FunctionDescriptor.of(
                        ValueLayout.ADDRESS,
                        ValueLayout.ADDRESS, ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG,
                        ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS, ValueLayout.JAVA_INT
                )
        );
        this.cgBitmapContextGetData = linker.downcallHandle(
                fw.lookup.find("CGBitmapContextGetData").orElseThrow(),
                FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS)
        );
        this.cgContextDrawImage = linker.downcallHandle(
                fw.lookup.find("CGContextDrawImage").orElseThrow(),
                FunctionDescriptor.ofVoid(
                        ValueLayout.ADDRESS,
                        ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE,
                        ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE,
                        ValueLayout.ADDRESS
                )
        );
        this.cgContextRelease = linker.downcallHandle(
                fw.lookup.find("CGContextRelease").orElseThrow(),
                FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
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

            MemorySegment colorSpace = (MemorySegment) this.fw.cgColorSpaceCreateDeviceRGB.invokeExact();
            MemorySegment ctx = (MemorySegment) this.cgBitmapContextCreate.invokeExact(
                    MemorySegment.NULL, w, h, 8L, w * 4, colorSpace, MacOSFrameworks.kCGImageAlphaPremultipliedLast
            );

            if (ctx.address() == 0) {
                this.fw.cgColorSpaceRelease.invokeExact(colorSpace);
                this.fw.cgImageRelease.invokeExact(cgImage);
                this.fw.cfRelease.invokeExact(source);
                this.fw.cfRelease.invokeExact(cfData);
                throw new IllegalStateException("CGBitmapContextCreate failed");
            }

            this.cgContextDrawImage.invokeExact(ctx, 0.0, 0.0, (double) w, (double) h, cgImage);

            MemorySegment pixelPtr = (MemorySegment) this.cgBitmapContextGetData.invokeExact(ctx);
            byte[] rgbaOut = pixelPtr.reinterpret(w * h * 4).toArray(ValueLayout.JAVA_BYTE);

            MacOSFrameworks.unpremultiplyAlpha(rgbaOut);

            this.cgContextRelease.invokeExact(ctx);
            this.fw.cgColorSpaceRelease.invokeExact(colorSpace);
            this.fw.cgImageRelease.invokeExact(cgImage);
            this.fw.cfRelease.invokeExact(source);
            this.fw.cfRelease.invokeExact(cfData);

            return new DecodedImage(rgbaOut, (int) w, (int) h);
        } catch (RuntimeException | Error e) {
            throw e;
        } catch (Throwable t) {
            throw new RuntimeException("macOS ImageIO decode failed", t);
        }
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
