module platformtools {
    exports org.redlance.platformtools;

    requires jfa;
    requires com.sun.jna;
    requires static java.desktop;
    requires static org.jetbrains.annotations;

    opens org.redlance.platformtools.impl.windows.jna to com.sun.jna;
    opens org.redlance.platformtools.impl.macos.appkit to jfa, com.sun.jna;
}