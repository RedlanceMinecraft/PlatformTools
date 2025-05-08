package org.redlance.platformtools.impl.windows.jna;

import com.sun.jna.IntegerType;
import com.sun.jna.Native;

public class WPARAM extends IntegerType {
    public WPARAM() {
        super(Native.POINTER_SIZE);
    }

    public WPARAM(long value) {
        super(Native.POINTER_SIZE, value, true);
    }
}
