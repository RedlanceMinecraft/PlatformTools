module platformtools.common {
    exports org.redlance.platformtools;
    exports org.redlance.platformtools.impl.macos.appkit;

    requires transitive com.sun.jna;
    requires transitive javaobjectivecbridge;
    requires static org.jetbrains.annotations;

    opens org.redlance.platformtools.impl.macos.appkit to com.sun.jna;
}
