package org.redlance.platformtools.impl.macos.appkit;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import de.jangassen.jfa.foundation.ID;

public interface CoreServices extends Library {
    CoreServices INSTANCE = Native.load(
            "/System/Library/Frameworks/CoreServices.framework/CoreServices",
            CoreServices.class);

    ID MDItemCreate(Pointer allocator, ID path);
    ID MDItemCopyAttribute(ID item, ID name);
}
