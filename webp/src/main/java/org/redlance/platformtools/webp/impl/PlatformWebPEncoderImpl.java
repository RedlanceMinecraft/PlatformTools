package org.redlance.platformtools.webp.impl;

import org.jetbrains.annotations.Nullable;
import org.redlance.platformtools.webp.encoder.PlatformWebPEncoder;
import org.redlance.platformtools.webp.impl.libwebp.LibWebPEncoder;

public class PlatformWebPEncoderImpl implements PlatformWebPEncoder {
    private final @Nullable PlatformWebPEncoder delegate = createBackend();

    private PlatformWebPEncoder requireDelegate() {
        if (this.delegate == null) throw new UnsupportedOperationException("No WebP encoder backend available");
        return this.delegate;
    }

    @Override
    public String backendName() {
        return requireDelegate().backendName();
    }

    @Override
    public byte[] encodeLossless(int[] argb, int width, int height) {
        return requireDelegate().encodeLossless(argb, width, height);
    }

    @Override
    public byte[] encodeLossy(int[] argb, int width, int height, float quality) {
        return requireDelegate().encodeLossy(argb, width, height, quality);
    }

    @Override
    public boolean isAvailable() {
        return this.delegate != null && this.delegate.isAvailable();
    }

    private static @Nullable PlatformWebPEncoder createBackend() {
        // 1. libwebp (all platforms)
        try {
            PlatformWebPEncoder backend = LibWebPEncoder.tryCreate();
            if (backend != null) return backend;
        } catch (Throwable ignored) {
        }

        // 2. System frameworks
        /*String os = System.getProperty("os.name", "").toLowerCase();

        if (os.contains("mac")) {
            try {
                PlatformWebPEncoder backend = MacOSImageIOEncoder.tryCreate();
                if (backend != null) return backend;
            } catch (Throwable ignored) {
            }
        } else if (os.contains("win")) {
            try {
                PlatformWebPEncoder backend = WindowsCodecsEncoder.tryCreate();
                if (backend != null) return backend;
            } catch (Throwable ignored) {
            }
        }*/

        // No pure-Java fallback encoder available
        return null;
    }
}
