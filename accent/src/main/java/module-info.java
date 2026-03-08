module platformtools.accent {
    exports org.redlance.platformtools.accent;

    requires transitive platformtools.common;
    requires static java.desktop;
    requires static org.jetbrains.annotations;

    opens org.redlance.platformtools.accent.impl.windows.jna to com.sun.jna;
    opens org.redlance.platformtools.accent.impl.windows to com.sun.jna;
    opens org.redlance.platformtools.accent.impl.macos to javaobjectivecbridge, com.sun.jna;
}
