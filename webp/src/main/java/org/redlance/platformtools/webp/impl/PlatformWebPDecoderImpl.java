package org.redlance.platformtools.webp.impl;

import org.jetbrains.annotations.Nullable;
import org.redlance.platformtools.webp.decoder.DecodedImage;
import org.redlance.platformtools.webp.decoder.PlatformWebPDecoder;
import org.redlance.platformtools.webp.impl.ngengine.NgEngineDecoder;
import org.redlance.platformtools.webp.impl.libwebp.LibWebPDecoder;
import org.redlance.platformtools.webp.impl.macos.MacOSImageIODecoder;
import org.redlance.platformtools.webp.impl.windows.WindowsCodecsDecoder;

public final class PlatformWebPDecoderImpl implements PlatformWebPDecoder {
    private final @Nullable PlatformWebPDecoder delegate = createBackend();

    private PlatformWebPDecoder requireDelegate() {
        if (this.delegate == null) throw new UnsupportedOperationException("No WebP decoder backend available");
        return this.delegate;
    }

    @Override
    public String backendName() {
        return requireDelegate().backendName();
    }

    @Override
    public DecodedImage decode(byte[] webpData) {
        return requireDelegate().decode(webpData);
    }

    @Override
    public int[] getInfo(byte[] webpData) {
        return requireDelegate().getInfo(webpData);
    }

    @Override
    public boolean isAvailable() {
        return this.delegate != null && this.delegate.isAvailable();
    }

    private static @Nullable PlatformWebPDecoder createBackend() {
        // 1. libwebp (all platforms)
        try {
            PlatformWebPDecoder backend = LibWebPDecoder.tryCreate();
            if (backend != null) return backend;
        } catch (Throwable ignored) {
        }

        // 2. System frameworks
        String os = System.getProperty("os.name", "").toLowerCase();

        if (os.contains("mac")) {
            try {
                PlatformWebPDecoder backend = MacOSImageIODecoder.tryCreate();
                if (backend != null) return backend;
            } catch (Throwable ignored) {
            }
        } else if (os.contains("win")) {
            try {
                PlatformWebPDecoder backend = WindowsCodecsDecoder.tryCreate();
                if (backend != null) return backend;
            } catch (Throwable ignored) {
            }
        }

        // 3. Pure-Java fallback (ngengine image-webp-java)
        try {
            PlatformWebPDecoder backend = NgEngineDecoder.tryCreate();
            if (backend != null) return backend;
        } catch (Throwable ignored) {
        }

        return null;
    }
}
