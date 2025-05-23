package org.redlance.platformtools.impl.unix;

import com.sun.jna.Library;
import com.sun.jna.Native;

public interface LibC extends Library {
    LibC INSTANCE = Native.load("c", LibC.class);

    long getxattr(String path, String name, byte[] value, long size, int position, int options);
}
