package org.redlance.platformtools.webp.decoder;

import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Decoded image with raw pixel data.
 */
@SuppressWarnings("unused") // API
public final class DecodedImage {
    private final int[] argb;
    private final int width;
    private final int height;
    private byte @Nullable [] png;

    /**
     * Creates a decoded image from packed ARGB pixels.
     *
     * @param argb   pixel data as packed ARGB integers ({@code (a << 24) | (r << 16) | (g << 8) | b}),
     *               length must be {@code width * height}
     * @param width  image width in pixels
     * @param height image height in pixels
     */
    public DecodedImage(int[] argb, int width, int height) {
        this.argb = argb;
        this.width = width;
        this.height = height;
    }

    /**
     * Parses a PNG image into a {@link DecodedImage}.
     *
     * @param pngData raw PNG file bytes
     * @return decoded image with cached PNG bytes
     * @throws IOException if the data is not a valid PNG or decoding fails
     */
    public static DecodedImage fromPng(byte[] pngData) throws IOException {
        try (ByteArrayInputStream is = new ByteArrayInputStream(pngData)) {
            BufferedImage img = ImageIO.read(is);
            if (img == null) throw new IOException("Failed to decode PNG");

            int w = img.getWidth();
            int h = img.getHeight();
            DecodedImage decoded = new DecodedImage(img.getRGB(0, 0, w, h, null, 0, w), w, h);
            decoded.png = pngData;
            return decoded;
        }
    }

    public int[] argb() {
        return this.argb;
    }

    public int width() {
        return this.width;
    }

    public int height() {
        return this.height;
    }

    /**
     * Returns {@code true} if PNG bytes are already cached.
     */
    public boolean isPngCached() {
        return this.png != null;
    }

    /**
     * Converts this image to PNG format. Cached after the first call.
     *
     * @return PNG file bytes
     * @throws IOException if encoding fails
     */
    public byte[] toPng() throws IOException {
        if (this.png != null) return this.png;

        BufferedImage img = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_ARGB);
        img.setRGB(0, 0, this.width, this.height, this.argb, 0, this.width);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ImageIO.write(img, "png", out);
            return this.png = out.toByteArray();
        }
    }
}
