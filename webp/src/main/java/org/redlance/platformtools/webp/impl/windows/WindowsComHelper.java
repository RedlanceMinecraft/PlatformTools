package org.redlance.platformtools.webp.impl.windows;

import org.jetbrains.annotations.Nullable;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;

public final class WindowsComHelper {
    static final Linker LINKER = Linker.nativeLinker();
    static final long PTR_SIZE = ValueLayout.ADDRESS.byteSize();

    // COM vtable indices — shared
    static final int IUNKNOWN_RELEASE = 2;

    // Decoder vtable indices
    static final int FACTORY_CREATE_DECODER = 7;
    static final int FACTORY_CREATE_DECODER_FROM_STREAM = 4;
    static final int FACTORY_CREATE_FORMAT_CONVERTER = 10;
    static final int FACTORY_CREATE_STREAM = 14;
    static final int DECODER_GET_FRAME = 13;
    static final int BITMAP_SOURCE_GET_SIZE = 3;
    static final int BITMAP_SOURCE_COPY_PIXELS = 7;
    static final int FORMAT_CONVERTER_INITIALIZE = 8;
    static final int WIC_STREAM_INITIALIZE_FROM_MEMORY = 16;

    // Encoder vtable indices
    static final int FACTORY_CREATE_ENCODER = 8;
    static final int ENCODER_INITIALIZE = 3;
    static final int ENCODER_CREATE_NEW_FRAME = 10;
    static final int ENCODER_COMMIT = 11;
    static final int FRAME_ENCODE_INITIALIZE = 3;
    static final int FRAME_ENCODE_SET_SIZE = 4;
    static final int FRAME_ENCODE_SET_PIXEL_FORMAT = 6;
    static final int FRAME_ENCODE_WRITE_PIXELS = 10;
    static final int FRAME_ENCODE_COMMIT = 12;
    static final int ISTREAM_READ = 3;
    static final int ISTREAM_SEEK = 5;
    static final int ISTREAM_STAT = 12;

    private final MethodHandle coCreateInstance;
    final SymbolLookup ole32;

    private WindowsComHelper(SymbolLookup ole32) {
        this.ole32 = ole32;

        this.coCreateInstance = LINKER.downcallHandle(ole32.find("CoCreateInstance").orElseThrow(),
                FunctionDescriptor.of(
                        ValueLayout.JAVA_INT,
                        ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT,
                        ValueLayout.ADDRESS, ValueLayout.ADDRESS
                )
        );
    }

    private static class Holder {
        static final @Nullable WindowsComHelper INSTANCE = tryInit();
    }

    public static @Nullable WindowsComHelper getInstance() {
        return Holder.INSTANCE;
    }

    private static @Nullable WindowsComHelper tryInit() {
        try {
            SymbolLookup ole32 = SymbolLookup.libraryLookup("ole32", Arena.global());

            MethodHandle coInitEx = LINKER.downcallHandle(
                    ole32.find("CoInitializeEx").orElseThrow(),
                    FunctionDescriptor.of(
                            ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT
                    )
            );
            //noinspection unused — invokeExact requires matching return type
            int hr = (int) coInitEx.invokeExact(MemorySegment.NULL, 0);

            return new WindowsComHelper(ole32);
        } catch (Throwable t) {
            return null;
        }
    }

    @Nullable MemorySegment createFactory(Arena arena) throws Throwable {
        MemorySegment factoryPtr = arena.allocate(ValueLayout.ADDRESS);
        int hr = (int) coCreateInstance.invokeExact(
                guidWICImagingFactory(arena), MemorySegment.NULL,
                1 | 4, // CLSCTX_INPROC_SERVER | CLSCTX_LOCAL_SERVER
                guidIWICImagingFactory(arena), factoryPtr
        );
        if (hr < 0) return null;
        return factoryPtr.get(ValueLayout.ADDRESS, 0);
    }

    static int comCallInt(MemorySegment obj, int vtableIndex, FunctionDescriptor fd, Object... extraArgs) throws Throwable {
        MemorySegment vtable = obj.reinterpret(PTR_SIZE).get(ValueLayout.ADDRESS, 0).reinterpret(PTR_SIZE * (vtableIndex + 1));
        MemorySegment funcPtr = vtable.getAtIndex(ValueLayout.ADDRESS, vtableIndex);
        MethodHandle mh = LINKER.downcallHandle(funcPtr, fd);
        Object[] allArgs = new Object[extraArgs.length + 1];
        allArgs[0] = obj;
        System.arraycopy(extraArgs, 0, allArgs, 1, extraArgs.length);
        return (int) mh.invokeWithArguments(allArgs);
    }

    static MemorySegment comCreateObj(Arena arena, MemorySegment obj, int vtableIndex, FunctionDescriptor fd, Object... extraArgs) throws Throwable {
        MemorySegment ptr = arena.allocate(ValueLayout.ADDRESS);
        Object[] args = new Object[extraArgs.length + 1];
        System.arraycopy(extraArgs, 0, args, 0, extraArgs.length);
        args[extraArgs.length] = ptr;
        int hr = comCallInt(obj, vtableIndex, fd, args);
        checkHr(hr, "COM call");
        return ptr.get(ValueLayout.ADDRESS, 0);
    }

    static void checkHr(int hr, String operation) {
        if (hr < 0) throw new IllegalStateException(operation + " failed: HRESULT 0x" + Integer.toHexString(hr));
    }

    static void comRelease(MemorySegment obj) {
        try {
            comCallInt(obj, IUNKNOWN_RELEASE, FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS));
        } catch (Throwable ignored) {}
    }

    // --- GUID helpers ---

    static MemorySegment writeGuid(Arena arena, int d1, short d2, short d3, byte... d4) {
        MemorySegment seg = arena.allocate(16);
        seg.set(ValueLayout.JAVA_INT_UNALIGNED, 0, d1);
        seg.set(ValueLayout.JAVA_SHORT_UNALIGNED, 4, d2);
        seg.set(ValueLayout.JAVA_SHORT_UNALIGNED, 6, d3);
        MemorySegment.copy(MemorySegment.ofArray(d4), 0, seg, 8, 8);
        return seg;
    }

    static MemorySegment guidWICImagingFactory(Arena arena) {
        return writeGuid(arena, 0xcacaf262, (short) 0x9370, (short) 0x4615,
                (byte) 0xa1, (byte) 0x3b, (byte) 0x9f, (byte) 0x55,
                (byte) 0x39, (byte) 0xda, (byte) 0x4c, (byte) 0x0a);
    }

    static MemorySegment guidIWICImagingFactory(Arena arena) {
        return writeGuid(arena, 0xec5ec8a9, (short) 0xc395, (short) 0x4314,
                (byte) 0x9c, (byte) 0x77, (byte) 0x54, (byte) 0xd7,
                (byte) 0xa9, (byte) 0x35, (byte) 0xff, (byte) 0x70);
    }

    static MemorySegment guidWebpContainer(Arena arena) {
        return writeGuid(arena, 0xe094b0e2, (short) 0x67f2, (short) 0x45b3,
                (byte) 0xb0, (byte) 0xea, (byte) 0x11, (byte) 0x53,
                (byte) 0x37, (byte) 0xca, (byte) 0x7c, (byte) 0xf3);
    }

    static MemorySegment guidPixelFormat32bppBGRA(Arena arena) {
        return writeGuid(arena, 0x6fddc324, (short) 0x4e03, (short) 0x4bfe,
                (byte) 0xb1, (byte) 0x85, (byte) 0x3d, (byte) 0x77,
                (byte) 0x76, (byte) 0x8d, (byte) 0xc9, (byte) 0x0e);
    }
}
