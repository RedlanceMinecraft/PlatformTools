package org.redlance.platformtools.webp.impl.libwebp;

import org.jetbrains.annotations.Nullable;
import org.redlance.platformtools.webp.encoder.PlatformWebPEncoder;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;

public final class LibWebPEncoder implements PlatformWebPEncoder {
    private final LibWebPLibrary lib;

    // size_t WebPEncodeLosslessBGRA(const uint8_t* bgra, int w, int h, int stride, uint8_t** output)
    private final MethodHandle webPEncodeLosslessBGRA;
    // size_t WebPEncodeBGRA(const uint8_t* bgra, int w, int h, int stride, float quality, uint8_t** output)
    private final @Nullable MethodHandle webPEncodeBGRA;

    private LibWebPEncoder(LibWebPLibrary lib) {
        this.lib = lib;

        Linker linker = Linker.nativeLinker();

        this.webPEncodeLosslessBGRA = linker.downcallHandle(
                lib.lookup.find("WebPEncodeLosslessBGRA").orElseThrow(),
                FunctionDescriptor.of(
                        ValueLayout.JAVA_LONG, ValueLayout.ADDRESS,
                        ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT,
                        ValueLayout.ADDRESS
                )
        );
        this.webPEncodeBGRA = lib.lookup.find("WebPEncodeBGRA").map(symbol -> linker.downcallHandle(symbol,
                FunctionDescriptor.of(
                        ValueLayout.JAVA_LONG, ValueLayout.ADDRESS,
                        ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT,
                        ValueLayout.JAVA_FLOAT, ValueLayout.ADDRESS
                )
        )).orElse(null);
    }

    public static @Nullable LibWebPEncoder tryCreate() {
        LibWebPLibrary lib = LibWebPLibrary.getInstance();
        if (lib == null || lib.lookup.find("WebPEncodeLosslessBGRA").isEmpty()) return null;
        return new LibWebPEncoder(lib);
    }

    @Override
    public String backendName() {
        return this.webPEncodeBGRA != null ? "libwebp" : "libwebp (lossless-only)";
    }

    @Override
    public byte[] encodeLossless(int[] argb, int width, int height) {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment outputPtr = arena.allocate(ValueLayout.ADDRESS);

            MemorySegment argbSeg = arena.allocate((long) argb.length * 4);
            argbSeg.copyFrom(MemorySegment.ofArray(argb));

            long size = (long) this.webPEncodeLosslessBGRA.invokeExact(
                    argbSeg, width, height, width * 4, outputPtr
            );
            if (size == 0) {
                throw new IllegalStateException("WebPEncodeLosslessBGRA failed");
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
    public byte[] encodeLossy(int[] argb, int width, int height, float quality) {
        if (this.webPEncodeBGRA == null) return encodeLossless(argb, width, height);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment outputPtr = arena.allocate(ValueLayout.ADDRESS);

            MemorySegment argbSeg = arena.allocate((long) argb.length * 4);
            argbSeg.copyFrom(MemorySegment.ofArray(argb));

            long size = (long) this.webPEncodeBGRA.invokeExact(
                    argbSeg, width, height, width * 4, quality * 100.0f, outputPtr
            );
            if (size == 0) {
                throw new IllegalStateException("WebPEncodeBGRA failed");
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
