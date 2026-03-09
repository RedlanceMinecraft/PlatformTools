package org.redlance.platformtools.webp.impl.libwebp;

import org.jetbrains.annotations.Nullable;
import org.redlance.platformtools.webp.encoder.PlatformWebPEncoder;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;

public final class LibWebPEncoder implements PlatformWebPEncoder {
    private final LibWebPLibrary lib;

    // size_t WebPEncodeLosslessRGBA(const uint8_t* rgba, int w, int h, int stride, uint8_t** output)
    private final MethodHandle webPEncodeLosslessRGBA;
    // size_t WebPEncodeRGBA(const uint8_t* rgba, int w, int h, int stride, float quality, uint8_t** output)
    private final MethodHandle webPEncodeRGBA;

    private LibWebPEncoder(LibWebPLibrary lib) {
        this.lib = lib;

        Linker linker = Linker.nativeLinker();

        this.webPEncodeLosslessRGBA = linker.downcallHandle(
                lib.lookup.find("WebPEncodeLosslessRGBA").orElseThrow(),
                FunctionDescriptor.of(
                        ValueLayout.JAVA_LONG, ValueLayout.ADDRESS,
                        ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT,
                        ValueLayout.ADDRESS
                )
        );
        this.webPEncodeRGBA = linker.downcallHandle(
                lib.lookup.find("WebPEncodeRGBA").orElseThrow(),
                FunctionDescriptor.of(
                        ValueLayout.JAVA_LONG, ValueLayout.ADDRESS,
                        ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT,
                        ValueLayout.JAVA_FLOAT, ValueLayout.ADDRESS
                )
        );
    }

    public static @Nullable LibWebPEncoder tryCreate() {
        LibWebPLibrary lib = LibWebPLibrary.getInstance();
        return lib != null ? new LibWebPEncoder(lib) : null;
    }

    @Override
    public String backendName() {
        return "libwebp";
    }

    @Override
    public byte[] encodeLossless(byte[] rgba, int width, int height) {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment outputPtr = arena.allocate(ValueLayout.ADDRESS);

            MemorySegment rgbaSeg = arena.allocate(rgba.length);
            rgbaSeg.copyFrom(MemorySegment.ofArray(rgba));

            long size = (long) this.webPEncodeLosslessRGBA.invokeExact(
                    rgbaSeg, width, height, width * 4, outputPtr
            );
            if (size == 0) {
                throw new IllegalStateException("WebPEncodeLosslessRGBA failed");
            }

            MemorySegment encoded = outputPtr.get(ValueLayout.ADDRESS, 0).reinterpret(size);
            byte[] result = encoded.toArray(ValueLayout.JAVA_BYTE);

            this.lib.webPFree.invokeExact(outputPtr.get(ValueLayout.ADDRESS, 0));
            return result;
        } catch (RuntimeException | Error e) {
            throw e;
        } catch (Throwable t) {
            throw new RuntimeException("WebP lossless encode failed", t);
        }
    }

    @Override
    public byte[] encodeLossy(byte[] rgba, int width, int height, float quality) {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment outputPtr = arena.allocate(ValueLayout.ADDRESS);

            MemorySegment rgbaSeg = arena.allocate(rgba.length);
            rgbaSeg.copyFrom(MemorySegment.ofArray(rgba));

            long size = (long) this.webPEncodeRGBA.invokeExact(
                    rgbaSeg, width, height, width * 4, quality * 100.0f, outputPtr
            );
            if (size == 0) {
                throw new IllegalStateException("WebPEncodeRGBA failed");
            }

            MemorySegment encoded = outputPtr.get(ValueLayout.ADDRESS, 0).reinterpret(size);
            byte[] result = encoded.toArray(ValueLayout.JAVA_BYTE);

            this.lib.webPFree.invokeExact(outputPtr.get(ValueLayout.ADDRESS, 0));
            return result;
        } catch (RuntimeException | Error e) {
            throw e;
        } catch (Throwable t) {
            throw new RuntimeException("WebP lossy encode failed", t);
        }
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}
