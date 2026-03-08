module platformtools.favorites {
    exports org.redlance.platformtools.favorites;

    requires transitive platformtools.common;
    requires static org.jetbrains.annotations;

    opens org.redlance.platformtools.favorites.impl.macos to javaobjectivecbridge, com.sun.jna;
}
