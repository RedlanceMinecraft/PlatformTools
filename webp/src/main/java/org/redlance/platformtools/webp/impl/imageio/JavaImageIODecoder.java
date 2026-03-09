package org.redlance.platformtools.webp.impl.imageio;

import org.redlance.platformtools.webp.decoder.PlatformWebPDecoder;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Iterator;

/**
 * Fallback WebP decoder using {@link javax.imageio.ImageIO}.
 *
 * <p>Works when a WebP ImageIO plugin is available on the classpath
 * (e.g. TwelveMonkeys, or a JRE with built-in WebP support).
 */
public final class JavaImageIODecoder implements PlatformWebPDecoder {
    private JavaImageIODecoder() {
    }

    public static JavaImageIODecoder tryCreate() {
        Iterator<ImageReader> readers = ImageIO.getImageReadersByMIMEType("image/webp");
        return readers.hasNext() ? new JavaImageIODecoder() : null;
    }

    @Override
    public String backendName() {
        return "Java ImageIO";
    }

    @Override
    public DecodedImage decode(byte[] webpData) {
        try {
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(webpData));
            if (img == null) {
                throw new IllegalStateException("ImageIO returned null: unsupported or invalid WebP data");
            }

            int w = img.getWidth();
            int h = img.getHeight();
            byte[] rgba = toRGBA(img, w, h);

            return new DecodedImage(rgba, w, h);
        } catch (IOException e) {
            throw new RuntimeException("Java ImageIO decode failed", e);
        }
    }

    @Override
    public int[] getInfo(byte[] webpData) {
        try (ImageInputStream iis = ImageIO.createImageInputStream(new ByteArrayInputStream(webpData))) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
            if (!readers.hasNext()) {
                throw new IllegalStateException("No ImageIO reader for WebP data");
            }

            ImageReader reader = readers.next();
            try {
                reader.setInput(iis);
                return new int[]{reader.getWidth(0), reader.getHeight(0)};
            } finally {
                reader.dispose();
            }
        } catch (IOException e) {
            throw new RuntimeException("Java ImageIO getInfo failed", e);
        }
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    private static byte[] toRGBA(BufferedImage img, int w, int h) {
        if (img.getType() == BufferedImage.TYPE_4BYTE_ABGR) {
            byte[] abgr = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
            byte[] rgba = new byte[w * h * 4];
            for (int i = 0; i < w * h; i++) {
                int si = i * 4;
                rgba[si]     = abgr[si + 3]; // R
                rgba[si + 1] = abgr[si + 2]; // G
                rgba[si + 2] = abgr[si + 1]; // B
                rgba[si + 3] = abgr[si];     // A
            }
            return rgba;
        }

        // Generic path: convert any type via getRGB
        byte[] rgba = new byte[w * h * 4];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int argb = img.getRGB(x, y);
                int i = (y * w + x) * 4;
                rgba[i]     = (byte) ((argb >> 16) & 0xFF); // R
                rgba[i + 1] = (byte) ((argb >> 8) & 0xFF);  // G
                rgba[i + 2] = (byte) (argb & 0xFF);         // B
                rgba[i + 3] = (byte) ((argb >> 24) & 0xFF); // A
            }
        }
        return rgba;
    }
}