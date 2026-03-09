package org.redlance.platformtools.webp.impl.imageio;

import org.redlance.platformtools.webp.decoder.PlatformWebPDecoder;

import org.redlance.platformtools.webp.decoder.DecodedImage;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
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

            return new DecodedImage(img.getRGB(0, 0, w, h, null, 0, w), w, h);
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
    public boolean isWebP(byte[] data) {
        if (data == null || data.length == 0) return false;

        try (ImageInputStream iis = ImageIO.createImageInputStream(new ByteArrayInputStream(data))) {
            return iis != null && ImageIO.getImageReaders(iis).hasNext();
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

}