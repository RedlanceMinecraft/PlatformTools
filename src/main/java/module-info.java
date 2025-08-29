module platformtools {
    exports org.redlance.platformtools;

    requires javaobjectivecbridge;
    requires com.sun.jna;
    requires static java.desktop;
    requires static org.jetbrains.annotations;

    opens org.redlance.platformtools.impl.windows.jna to com.sun.jna;
    opens org.redlance.platformtools.impl.windows to com.sun.jna;

    opens org.redlance.platformtools.impl.macos.appkit to com.sun.jna;
    opens org.redlance.platformtools.impl.macos to javaobjectivecbridge, com.sun.jna;
}