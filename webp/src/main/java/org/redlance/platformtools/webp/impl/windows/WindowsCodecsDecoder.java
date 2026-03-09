package org.redlance.platformtools.webp.impl.windows;

import org.jetbrains.annotations.Nullable;
import org.redlance.platformtools.webp.decoder.DecodedImage;
import org.redlance.platformtools.webp.decoder.PlatformWebPDecoder;

import java.lang.foreign.*;

import static org.redlance.platformtools.webp.impl.windows.WindowsComHelper.*;

public final class WindowsCodecsDecoder implements PlatformWebPDecoder {
    private final WindowsComHelper com;

    private WindowsCodecsDecoder(WindowsComHelper com) {
        this.com = com;
    }

    public static @Nullable WindowsCodecsDecoder tryCreate() {
        WindowsComHelper com = WindowsComHelper.getInstance();
        if (com == null) return null;

        // Verify WebP decode support (codec may not be installed)
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment factory = com.createFactory(arena);
            if (factory == null) return null;

            try {
                MemorySegment decoder = comCreateObj(
                        arena, factory, FACTORY_CREATE_DECODER,
                        FunctionDescriptor.of(
                                ValueLayout.JAVA_INT,
                                ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS
                        ),
                        guidWebpContainer(arena), MemorySegment.NULL
                );
                comRelease(decoder);
            } finally {
                comRelease(factory);
            }
        } catch (Throwable t) {
            return null;
        }

        return new WindowsCodecsDecoder(com);
    }

    @Override
    public String backendName() {
        return "Windows WIC";
    }

    @Override
    public DecodedImage decode(byte[] webpData) {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment factory = this.com.createFactory(arena);
            if (factory == null) throw new IllegalStateException("Failed to create WIC factory");

            MemorySegment wicStream = null, decoder = null, frame = null, converter = null;
            try {
                wicStream = comCreateObj(
                        arena, factory, FACTORY_CREATE_STREAM,
                        FunctionDescriptor.of(
                                ValueLayout.JAVA_INT,
                                ValueLayout.ADDRESS, ValueLayout.ADDRESS
                        )
                );

                // InitializeFromMemory stores the pointer — must use arena-allocated memory
                MemorySegment dataSeg = arena.allocate(webpData.length);
                dataSeg.copyFrom(MemorySegment.ofArray(webpData));

                checkHr(comCallInt(
                        wicStream, WIC_STREAM_INITIALIZE_FROM_MEMORY,
                        FunctionDescriptor.of(
                                ValueLayout.JAVA_INT,
                                ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT
                        ),
                        dataSeg, webpData.length
                ), "InitializeFromMemory");

                decoder = comCreateObj(
                        arena, factory, FACTORY_CREATE_DECODER_FROM_STREAM,
                        FunctionDescriptor.of(
                                ValueLayout.JAVA_INT,
                                ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS,
                                ValueLayout.JAVA_INT, ValueLayout.ADDRESS
                        ),
                        wicStream, MemorySegment.NULL, 0
                );

                frame = comCreateObj(
                        arena, decoder, DECODER_GET_FRAME,
                        FunctionDescriptor.of(
                                ValueLayout.JAVA_INT,
                                ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS
                        ),
                        0
                );

                converter = comCreateObj(
                        arena, factory, FACTORY_CREATE_FORMAT_CONVERTER,
                        FunctionDescriptor.of(
                                ValueLayout.JAVA_INT,
                                ValueLayout.ADDRESS, ValueLayout.ADDRESS
                        )
                );

                checkHr(comCallInt(
                        converter, FORMAT_CONVERTER_INITIALIZE,
                        FunctionDescriptor.of(
                                ValueLayout.JAVA_INT,
                                ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS,
                                ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_DOUBLE,
                                ValueLayout.JAVA_INT
                        ),
                        frame, guidPixelFormat32bppBGRA(arena), 0, MemorySegment.NULL, 0.0, 0
                ), "FormatConverter.Initialize");

                MemorySegment wPtr = arena.allocate(ValueLayout.JAVA_INT);
                MemorySegment hPtr = arena.allocate(ValueLayout.JAVA_INT);
                checkHr(comCallInt(
                        converter, BITMAP_SOURCE_GET_SIZE,
                        FunctionDescriptor.of(
                                ValueLayout.JAVA_INT,
                                ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS
                        ),
                        wPtr, hPtr
                ), "GetSize");

                int w = wPtr.get(ValueLayout.JAVA_INT, 0);
                int h = hPtr.get(ValueLayout.JAVA_INT, 0);
                int stride = w * 4;
                int bufSize = stride * h;

                MemorySegment buffer = arena.allocate(bufSize);
                checkHr(comCallInt(
                        converter, BITMAP_SOURCE_COPY_PIXELS,
                        FunctionDescriptor.of(
                                ValueLayout.JAVA_INT,
                                ValueLayout.ADDRESS, ValueLayout.ADDRESS,
                                ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.ADDRESS
                        ),
                        MemorySegment.NULL, stride, bufSize, buffer
                ), "CopyPixels");

                return new DecodedImage(buffer.toArray(ValueLayout.JAVA_INT), w, h);
            } finally {
                if (converter != null) comRelease(converter);
                if (frame != null) comRelease(frame);
                if (decoder != null) comRelease(decoder);
                if (wicStream != null) comRelease(wicStream);
                comRelease(factory);
            }
        } catch (RuntimeException | Error e) {
            throw e;
        } catch (Throwable t) {
            throw new RuntimeException("WIC decode failed", t);
        }
    }

    @Override
    public int[] getInfo(byte[] webpData) {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment factory = this.com.createFactory(arena);
            if (factory == null) throw new IllegalStateException("Failed to create WIC factory");

            MemorySegment wicStream = null, decoder = null, frame = null;
            try {
                wicStream = comCreateObj(
                        arena, factory, FACTORY_CREATE_STREAM,
                        FunctionDescriptor.of(
                                ValueLayout.JAVA_INT,
                                ValueLayout.ADDRESS, ValueLayout.ADDRESS
                        )
                );

                // InitializeFromMemory stores the pointer — must use arena-allocated memory
                MemorySegment dataSeg = arena.allocate(webpData.length);
                dataSeg.copyFrom(MemorySegment.ofArray(webpData));

                checkHr(comCallInt(
                        wicStream, WIC_STREAM_INITIALIZE_FROM_MEMORY,
                        FunctionDescriptor.of(
                                ValueLayout.JAVA_INT,
                                ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT
                        ),
                        dataSeg, webpData.length
                ), "InitializeFromMemory");

                decoder = comCreateObj(
                        arena, factory, FACTORY_CREATE_DECODER_FROM_STREAM,
                        FunctionDescriptor.of(
                                ValueLayout.JAVA_INT,
                                ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS,
                                ValueLayout.JAVA_INT, ValueLayout.ADDRESS
                        ),
                        wicStream, MemorySegment.NULL, 0
                );

                frame = comCreateObj(
                        arena, decoder, DECODER_GET_FRAME,
                        FunctionDescriptor.of(
                                ValueLayout.JAVA_INT,
                                ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS
                        ),
                        0
                );

                MemorySegment wPtr = arena.allocate(ValueLayout.JAVA_INT);
                MemorySegment hPtr = arena.allocate(ValueLayout.JAVA_INT);
                checkHr(comCallInt(
                        frame, BITMAP_SOURCE_GET_SIZE,
                        FunctionDescriptor.of(
                                ValueLayout.JAVA_INT,
                                ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS
                        ),
                        wPtr, hPtr
                ), "GetSize");

                return new int[] {
                        wPtr.get(ValueLayout.JAVA_INT, 0),
                        hPtr.get(ValueLayout.JAVA_INT, 0)
                };
            } finally {
                if (frame != null) comRelease(frame);
                if (decoder != null) comRelease(decoder);
                if (wicStream != null) comRelease(wicStream);
                comRelease(factory);
            }
        } catch (RuntimeException | Error e) {
            throw e;
        } catch (Throwable t) {
            throw new RuntimeException("WIC getInfo failed", t);
        }
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}
