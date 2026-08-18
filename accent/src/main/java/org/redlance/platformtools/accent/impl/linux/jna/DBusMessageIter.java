package org.redlance.platformtools.accent.impl.linux.jna;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

@Structure.FieldOrder({
        "dummy1", "dummy2", "dummy3", "dummy4", "dummy5", "dummy6", "dummy7",
        "dummy8", "dummy9", "dummy10", "dummy11", "pad1", "pad2", "pad3"
})
public class DBusMessageIter extends Structure {
    public Pointer dummy1;
    public Pointer dummy2;
    public int dummy3;
    public int dummy4;
    public int dummy5;
    public int dummy6;
    public int dummy7;
    public int dummy8;
    public int dummy9;
    public int dummy10;
    public int dummy11;
    public int pad1;
    public Pointer pad2;
    public Pointer pad3;
}
