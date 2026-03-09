module platformtools.progress {
    exports org.redlance.platformtools.progress;

    requires transitive platformtools.common;
    requires static org.jetbrains.annotations;
    requires static java.desktop;

    opens org.redlance.platformtools.progress.impl.macos to javaobjectivecbridge, com.sun.jna;
}
