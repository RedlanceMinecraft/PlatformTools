package org.redlance.platformtools.webp.impl.libwebp;

import org.jetbrains.annotations.Nullable;
import org.redlance.platformtools.webp.decoder.PlatformWebPDecoder;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;

public final class LibWebPDecoder implements PlatformWebPDecoder {
    private final LibWebPLibrary lib;

    // uint8_t* WebPDecodeRGBA(const uint8_t* data, size_t size, int* w, int* h)
    private final MethodHandle webPDecodeRGBA;
    // int WebPGetInfo(const uint8_t* data, size_t size, int* w, int* h)
    private final MethodHandle webPGetInfo;

    private LibWebPDecoder(LibWebPLibrary lib) {
        this.lib = lib;

        Linker linker = Linker.nativeLinker();

        this.webPDecodeRGBA = linker.downcallHandle(
                lib.lookup.find("WebPDecodeRGBA").orElseThrow(),
                FunctionDescriptor.of(
                        ValueLayout.ADDRESS, ValueLayout.ADDRESS,
                        ValueLayout.JAVA_LONG, ValueLayout.ADDRESS, ValueLayout.ADDRESS
                )
        );
        this.webPGetInfo = linker.downcallHandle(
                lib.lookup.find("WebPGetInfo").orElseThrow(),
                FunctionDescriptor.of(
                        ValueLayout.JAVA_INT, ValueLayout.ADDRESS,
                        ValueLayout.JAVA_LONG, ValueLayout.ADDRESS, ValueLayout.ADDRESS
                )
        );
    }

    public static @Nullable LibWebPDecoder tryCreate() {
        LibWebPLibrary lib = LibWebPLibrary.getInstance();
        return lib != null ? new LibWebPDecoder(lib) : null;
    }

    @Override
    public String backendName() {
        return "libwebp";
    }

    @Override
    public DecodedImage decode(byte[] webpData) {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment wPtr = arena.allocate(ValueLayout.JAVA_INT);
            MemorySegment hPtr = arena.allocate(ValueLayout.JAVA_INT);

            MemorySegment dataSeg = arena.allocate(webpData.length);
            dataSeg.copyFrom(MemorySegment.ofArray(webpData));

            MemorySegment result = (MemorySegment) this.webPDecodeRGBA.invokeExact(
                    dataSeg, (long) webpData.length, wPtr, hPtr
            );
            if (result.equals(MemorySegment.NULL)) {
                throw new IllegalStateException("WebPDecodeRGBA failed: invalid or unsupported WebP data");
            }

            int w = wPtr.get(ValueLayout.JAVA_INT, 0);
            int h = hPtr.get(ValueLayout.JAVA_INT, 0);

            byte[] pixels = result.reinterpret((long) w * h * 4).toArray(ValueLayout.JAVA_BYTE);
            this.lib.webPFree.invokeExact(result);

            return new DecodedImage(pixels, w, h);
        } catch (RuntimeException | Error e) {
            throw e;
        } catch (Throwable t) {
            throw new RuntimeException("WebP decode failed", t);
        }
    }

    @Override
    public int[] getInfo(byte[] webpData) {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment wPtr = arena.allocate(ValueLayout.JAVA_INT);
            MemorySegment hPtr = arena.allocate(ValueLayout.JAVA_INT);

            MemorySegment dataSeg = arena.allocate(webpData.length);
            dataSeg.copyFrom(MemorySegment.ofArray(webpData));

            int ok = (int) this.webPGetInfo.invokeExact(
                    dataSeg, (long) webpData.length, wPtr, hPtr
            );
            if (ok == 0) {
                throw new IllegalStateException("WebPGetInfo failed: invalid or unsupported WebP data");
            }

            return new int[] {
                    wPtr.get(ValueLayout.JAVA_INT, 0),
                    hPtr.get(ValueLayout.JAVA_INT, 0)
            };
        } catch (RuntimeException | Error e) {
            throw e;
        } catch (Throwable t) {
            throw new RuntimeException("WebP getInfo failed", t);
        }
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}
