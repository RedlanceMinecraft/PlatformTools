package org.redlance.platformtools.impl.macos.appkit;

import com.sun.jna.Pointer;
import de.jangassen.jfa.ObjcToJava;
import de.jangassen.jfa.annotation.NamedArg;
import de.jangassen.jfa.appkit.NSObject;
import de.jangassen.jfa.appkit.NSString;
import de.jangassen.jfa.foundation.ID;

public interface NSDistributedNotificationCenter extends NSObject {
    static NSDistributedNotificationCenter defaultCenter() {
        return ObjcToJava.invokeStatic(NSDistributedNotificationCenter.class, "defaultCenter");
    }

    void addObserver(ID observer, @NamedArg("selector") Pointer aSelector, @NamedArg("name") String aName, @NamedArg("object") NSString anObject);
}
