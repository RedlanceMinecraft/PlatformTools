module platformtools.webp {
    exports org.redlance.platformtools.webp.decoder;
    exports org.redlance.platformtools.webp.encoder;

    exports org.redlance.platformtools.webp.impl.libwebp to platformtools.testing;
    exports org.redlance.platformtools.webp.impl.macos to platformtools.testing;
    exports org.redlance.platformtools.webp.impl.windows to platformtools.testing;
    exports org.redlance.platformtools.webp.impl.ngengine to platformtools.testing;

    requires java.desktop;
    requires static org.jetbrains.annotations;
}
