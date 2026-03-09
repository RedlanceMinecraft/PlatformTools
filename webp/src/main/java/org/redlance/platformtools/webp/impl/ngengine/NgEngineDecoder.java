package org.redlance.platformtools.webp.impl.ngengine;

import org.jetbrains.annotations.Nullable;
import org.ngengine.webp.decoder.WebPDecoder;
import org.redlance.platformtools.webp.decoder.DecodedImage;
import org.redlance.platformtools.webp.decoder.PlatformWebPDecoder;

import java.nio.ByteBuffer;

/**
 * Fallback WebP decoder using ngengine image-webp-java (pure-Java).
 */
public final class NgEngineDecoder implements PlatformWebPDecoder {
    private NgEngineDecoder() {
    }

    public static @Nullable NgEngineDecoder tryCreate() {
        try {
            // Verify the library is on the classpath
            Class.forName("org.ngengine.webp.decoder.WebPDecoder");
            return new NgEngineDecoder();
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    @Override
    public String backendName() {
        return "Java (ngengine)";
    }

    @Override
    public DecodedImage decode(byte[] webpData) {
        try {
            var decoded = WebPDecoder.decode(webpData);
            ByteBuffer rgba = decoded.rgba;
            rgba.rewind();

            int pixelCount = decoded.width * decoded.height;
            int[] argb = new int[pixelCount];
            for (int i = 0; i < pixelCount; i++) {
                int r = rgba.get() & 0xFF;
                int g = rgba.get() & 0xFF;
                int b = rgba.get() & 0xFF;
                int a = rgba.get() & 0xFF;
                argb[i] = (a << 24) | (r << 16) | (g << 8) | b;
            }

            return new DecodedImage(argb, decoded.width, decoded.height);
        } catch (Exception e) {
            throw new IllegalStateException("ngengine WebP decode failed", e);
        }
    }

    @Override
    public int[] getInfo(byte[] webpData) {
        try {
            var decoded = WebPDecoder.decode(webpData);
            return new int[]{decoded.width, decoded.height};
        } catch (Exception e) {
            throw new IllegalStateException("ngengine WebP getInfo failed", e);
        }
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}
