package org.redlance.platformtools.impl.macos.appkit;

import de.jangassen.jfa.ObjcToJava;
import de.jangassen.jfa.appkit.NSObject;

@SuppressWarnings("unused")
public interface NSColorSpace extends NSObject {
    static NSColorSpace genericRGBColorSpace() {
        return ObjcToJava.invokeStatic(NSColorSpace.class, "genericRGBColorSpace");
    }
}
