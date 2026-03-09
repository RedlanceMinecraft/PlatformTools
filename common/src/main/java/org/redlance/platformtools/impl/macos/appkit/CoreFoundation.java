package org.redlance.platformtools.impl.macos.appkit;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

public interface CoreFoundation extends Library {
    CoreFoundation INSTANCE = Native.load("CoreFoundation", CoreFoundation.class);

    long CFArrayGetCount(Pointer array);
    Pointer CFArrayGetValueAtIndex(Pointer array, long idx);
}
