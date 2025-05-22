module platformtools {
    exports org.redlance.platformtools;

    requires jfa;
    requires com.sun.jna;
    requires java.desktop;
    requires org.jetbrains.annotations;

    opens org.redlance.platformtools.impl to jfa;
}