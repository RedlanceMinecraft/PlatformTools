package org.redlance.platformtools.impl.macos.appkit;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

public interface CoreServices extends Library {
    CoreServices INSTANCE = Native.load("CoreServices", CoreServices.class);

    Pointer MDItemCreate(Pointer allocator, Pointer path);
    Pointer MDItemCopyAttribute(Pointer item, Pointer name);
}
