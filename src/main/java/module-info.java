module platformtools {
    exports org.redlance.platformtools;
    exports org.redlance.platformtools.impl;

    requires com.sun.jna;
    requires com.sun.jna.platform;

    requires java.desktop;
    requires jfa.main.SNAPSHOT;
}