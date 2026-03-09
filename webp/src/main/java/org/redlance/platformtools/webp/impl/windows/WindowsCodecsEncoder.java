package org.redlance.platformtools.webp.impl.windows;

import org.jetbrains.annotations.Nullable;
import org.redlance.platformtools.webp.encoder.PlatformWebPEncoder;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;

import static org.redlance.platformtools.webp.impl.windows.WindowsComHelper.*;

public final class WindowsCodecsEncoder implements PlatformWebPEncoder {
    private final WindowsComHelper com;

    // Encoder-only handle
    private final MethodHandle createStreamOnHGlobal;

    private WindowsCodecsEncoder(WindowsComHelper com) {
        this.com = com;

        this.createStreamOnHGlobal = LINKER.downcallHandle(
                com.ole32.find("CreateStreamOnHGlobal").orElseThrow(),
                FunctionDescriptor.of(
                        ValueLayout.JAVA_INT,
                        ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS
                )
        );
    }

    public static @Nullable WindowsCodecsEncoder tryCreate() {
        WindowsComHelper com = WindowsComHelper.getInstance();
        if (com == null) return null;

        // Verify WebP encode support
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment factory = com.createFactory(arena);
            if (factory == null) return null;

            try {
                MemorySegment encoder = comCreateObj(
                        arena, factory, FACTORY_CREATE_ENCODER,
                        FunctionDescriptor.of(
                                ValueLayout.JAVA_INT,
                                ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS
                        ),
                        guidWebpContainer(arena), MemorySegment.NULL
                );
                comRelease(encoder);
            } finally {
                comRelease(factory);
            }
        } catch (Throwable t) {
            return null;
        }

        return new WindowsCodecsEncoder(com);
    }

    @Override
    public String backendName() {
        return "Windows WIC";
    }

    @Override
    public byte[] encodeLossless(int[] argb, int width, int height) {
        return encodeLossy(argb, width, height, 1.0f);
    }

    @Override
    public byte[] encodeLossy(int[] argb, int width, int height, float quality) {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment factory = this.com.createFactory(arena);
            if (factory == null) throw new IllegalStateException("Failed to create WIC factory");

            MemorySegment stream = null, encoder = null, frame = null, props = null;
            try {
                MemorySegment streamPtr = arena.allocate(ValueLayout.ADDRESS);
                checkHr((int) this.createStreamOnHGlobal.invokeExact(
                        MemorySegment.NULL, 1, streamPtr
                ), "CreateStreamOnHGlobal");
                stream = streamPtr.get(ValueLayout.ADDRESS, 0);

                encoder = comCreateObj(
                        arena, factory, FACTORY_CREATE_ENCODER,
                        FunctionDescriptor.of(
                                ValueLayout.JAVA_INT,
                                ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS
                        ),
                        guidWebpContainer(arena), MemorySegment.NULL
                );

                checkHr(comCallInt(
                        encoder, ENCODER_INITIALIZE,
                        FunctionDescriptor.of(
                                ValueLayout.JAVA_INT,
                                ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT
                        ),
                        stream, 2
                ), "Encoder.Initialize");

                MemorySegment framePtr = arena.allocate(ValueLayout.ADDRESS);
                MemorySegment propsPtr = arena.allocate(ValueLayout.ADDRESS);
                checkHr(comCallInt(
                        encoder, ENCODER_CREATE_NEW_FRAME,
                        FunctionDescriptor.of(
                                ValueLayout.JAVA_INT,
                                ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS
                        ),
                        framePtr, propsPtr
                ), "CreateNewFrame");
                frame = framePtr.get(ValueLayout.ADDRESS, 0);
                props = propsPtr.get(ValueLayout.ADDRESS, 0);

                checkHr(comCallInt(
                        frame, FRAME_ENCODE_INITIALIZE,
                        FunctionDescriptor.of(
                                ValueLayout.JAVA_INT,
                                ValueLayout.ADDRESS, ValueLayout.ADDRESS
                        ),
                        props
                ), "FrameEncode.Initialize");

                checkHr(comCallInt(
                        frame, FRAME_ENCODE_SET_SIZE,
                        FunctionDescriptor.of(
                                ValueLayout.JAVA_INT,
                                ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT
                        ),
                        width, height
                ), "SetSize");

                checkHr(comCallInt(
                        frame, FRAME_ENCODE_SET_PIXEL_FORMAT,
                        FunctionDescriptor.of(
                                ValueLayout.JAVA_INT,
                                ValueLayout.ADDRESS, ValueLayout.ADDRESS
                        ),
                        guidPixelFormat32bppBGRA(arena)
                ), "SetPixelFormat");

                int stride = width * 4;

                int bufSize = argb.length * 4;
                MemorySegment argbSeg = arena.allocate(bufSize);
                argbSeg.copyFrom(MemorySegment.ofArray(argb));

                checkHr(comCallInt(
                        frame, FRAME_ENCODE_WRITE_PIXELS,
                        FunctionDescriptor.of(
                                ValueLayout.JAVA_INT,
                                ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT,
                                ValueLayout.JAVA_INT, ValueLayout.ADDRESS
                        ),
                        height, stride, bufSize, argbSeg
                ), "WritePixels");

                checkHr(comCallInt(
                        frame, FRAME_ENCODE_COMMIT,
                        FunctionDescriptor.of(
                                ValueLayout.JAVA_INT, ValueLayout.ADDRESS
                        )
                ), "FrameEncode.Commit");

                checkHr(comCallInt(
                        encoder, ENCODER_COMMIT,
                        FunctionDescriptor.of(
                                ValueLayout.JAVA_INT, ValueLayout.ADDRESS
                        )
                ), "Encoder.Commit");

                return readStream(arena, stream);
            } finally {
                if (props != null && props.address() != 0) comRelease(props);
                if (frame != null) comRelease(frame);
                if (encoder != null) comRelease(encoder);
                if (stream != null) comRelease(stream);
                comRelease(factory);
            }
        } catch (RuntimeException | Error e) {
            throw e;
        } catch (Throwable t) {
            throw new RuntimeException("WIC encode failed", t);
        }
    }

    private byte[] readStream(Arena arena, MemorySegment stream) throws Throwable {
        // Seek to 0
        comCallInt(
                stream, ISTREAM_SEEK,
                FunctionDescriptor.of(
                        ValueLayout.JAVA_INT,
                        ValueLayout.ADDRESS, ValueLayout.JAVA_LONG, ValueLayout.JAVA_INT, ValueLayout.ADDRESS
                ),
                0L, 0, MemorySegment.NULL // STREAM_SEEK_SET = 0
        );

        // Stat to get size
        MemorySegment statstg = arena.allocate(72); // sizeof(STATSTG)
        checkHr(comCallInt(
                stream, ISTREAM_STAT,
                FunctionDescriptor.of(
                        ValueLayout.JAVA_INT,
                        ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT
                ),
                statstg, 1 // STATFLAG_NONAME = 1
        ), "IStream.Stat");

        long size = statstg.get(ValueLayout.JAVA_LONG, 8); // cbSize at offset 8
        if (size <= 0 || size > Integer.MAX_VALUE) {
            throw new IllegalStateException("Invalid stream size: " + size);
        }

        // Read
        MemorySegment buffer = arena.allocate(size);
        MemorySegment bytesRead = arena.allocate(ValueLayout.JAVA_INT);
        checkHr(comCallInt(
                stream, ISTREAM_READ,
                FunctionDescriptor.of(
                        ValueLayout.JAVA_INT,
                        ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS
                ),
                buffer, (int) size, bytesRead
        ), "IStream.Read");

        int read = bytesRead.get(ValueLayout.JAVA_INT, 0);
        return buffer.asSlice(0, read).toArray(ValueLayout.JAVA_BYTE);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}
