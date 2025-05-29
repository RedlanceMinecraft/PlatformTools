package org.redlance.platformtools.impl.macos.appkit;

import com.sun.jna.ptr.DoubleByReference;
import de.jangassen.jfa.ObjcToJava;
import de.jangassen.jfa.annotation.NamedArg;
import de.jangassen.jfa.appkit.NSObject;

@SuppressWarnings("unused")
public interface NSColor extends NSObject {
    static NSColor controlAccentColor() {
        return ObjcToJava.invokeStatic(NSColor.class, "controlAccentColor");
    }

    void getRed(
            @NamedArg("red") DoubleByReference red,
            @NamedArg("green") DoubleByReference green,
            @NamedArg("blue") DoubleByReference blue,
            @NamedArg("alpha") DoubleByReference alpha
    );

    NSColor colorUsingColorSpace(NSColorSpace space);
}
