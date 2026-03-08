module platformtools.referer {
    exports org.redlance.platformtools.referer;

    requires transitive platformtools.common;
    requires static org.jetbrains.annotations;

    opens org.redlance.platformtools.referer.impl.macos to javaobjectivecbridge, com.sun.jna;
}
