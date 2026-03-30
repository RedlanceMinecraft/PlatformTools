package org.redlance.platformtools.webp.decoder;

import org.jetbrains.annotations.Nullable;
import org.redlance.platformtools.webp.impl.image.DecodedImageImpl;
import org.redlance.platformtools.webp.impl.image.LazyDecodedImage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * Decoded image with raw pixel data.
 *
 * <p>Can be created in three ways:
 * <ul>
 *     <li>{@link #ofArgb(int[], int, int)} — from pre-decoded ARGB pixels (eager)</li>
 *     <li>{@link #fromWebP(byte[])} — from raw WebP bytes (lazy, decoded on first access)</li>
 *     <li>{@link #fromPng(byte[])} — from raw PNG bytes (eager, with cached PNG output)</li>
 * </ul>
 *
 * <p>All variants support WebP byte caching via {@link #cacheWebP(Float, byte[])}
 * and retrieval via {@link #toWebP(Float)}.
 * Use {@link #isDecoded()} to check whether pixel data has been materialized.
 */
@SuppressWarnings("unused") // API
public abstract sealed class DecodedImage permits LazyDecodedImage, DecodedImageImpl {
    private byte @Nullable [] png;

    private Float webpQuality;
    private byte @Nullable [] webp;

    /**
     * Creates a decoded image from packed ARGB pixels.
     *
     * @param argb   pixel data as packed ARGB integers ({@code (a << 24) | (r << 16) | (g << 8) | b}),
     *               length must be {@code width * height}
     * @param width  image width in pixels
     * @param height image height in pixels
     * @return decoded image with immediately available pixel data
     */
    public static DecodedImage ofArgb(int[] argb, int width, int height) {
        return new DecodedImageImpl(argb, width, height);
    }

    /**
     * Wraps raw WebP bytes for on-demand decoding.
     * Pixel data will not be decoded until {@link #argb()}, {@link #width()},
     * {@link #height()} or {@link #toPng()} is called.
     *
     * @param webpData raw WebP file bytes
     * @return decoded image with deferred pixel decoding
     */
    public static DecodedImage fromWebP(byte[] webpData) {
        return new LazyDecodedImage(webpData);
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
            DecodedImage decoded = fromPng(is);
            decoded.png = pngData;
            return decoded;
        }
    }

    /**
     * Parses a PNG image from an {@link InputStream}.
     * PNG bytes are not cached (unlike {@link #fromPng(byte[])}).
     *
     * @param is input stream containing PNG data
     * @return decoded image
     * @throws IOException if the data is not a valid PNG or decoding fails
     */
    public static DecodedImage fromPng(InputStream is) throws IOException {
        BufferedImage img = ImageIO.read(is);
        if (img == null) throw new IOException("Failed to decode PNG");

        int w = img.getWidth();
        int h = img.getHeight();
        return ofArgb(img.getRGB(0, 0, w, h, null, 0, w), w, h);
    }

    /**
     * Returns the ARGB pixel data as packed integers.
     * If created via {@code fromWebP}, triggers decoding on first call.
     *
     * @return pixel data as packed ARGB integers, length is {@code width() * height()}
     */
    public abstract int[] argb();

    /**
     * Returns the image width in pixels.
     * If created via {@code fromWebP}, triggers decoding on first call.
     */
    public abstract int width();

    /**
     * Returns the image height in pixels.
     * If created via {@code fromWebP}, triggers decoding on first call.
     */
    public abstract int height();

    /**
     * Returns {@code true} if the pixel data has been decoded.
     * Always {@code true} for images created via {@link #ofArgb(int[], int, int)}
     * or {@link #fromPng(byte[])}.
     */
    public abstract boolean isDecoded();

    /**
     * Returns {@code true} if PNG bytes are already cached.
     */
    public final boolean isPngCached() {
        return this.png != null;
    }

    /**
     * Converts this image to PNG format. Cached after the first call.
     *
     * @return PNG file bytes
     * @throws IOException if encoding fails
     */
    public final byte[] toPng() throws IOException {
        if (this.png != null) return this.png;

        int[] pixels = argb();
        BufferedImage img = new BufferedImage(this.width(), this.height(), BufferedImage.TYPE_INT_ARGB);
        img.setRGB(0, 0, this.width(), this.height(), pixels, 0, this.width());

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ImageIO.write(img, "png", out);
            return this.png = out.toByteArray();
        }
    }

    /**
     * Returns cached WebP bytes if available and encoded with the given quality.
     *
     * @param quality the expected encoding quality ({@code null} for lossless)
     * @return raw WebP file bytes, or {@code null} if not cached or quality doesn't match
     */
    public final byte @Nullable [] toWebP(@Nullable Float quality) {
        if (this.webp == null || !Objects.equals(quality, this.webpQuality)) return null;
        return this.webp;
    }

    /**
     * Caches encoded WebP bytes for later retrieval via {@link #toWebP(Float)}.
     * If the cache already contains bytes with the same quality, this is a no-op.
     *
     * @param quality the quality used for encoding ({@code null} for lossless)
     * @param webp    the encoded WebP bytes
     */
    public final void cacheWebP(@Nullable Float quality, byte[] webp) {
        if (this.webp == null || !Objects.equals(quality, this.webpQuality)) {
            this.webpQuality = quality;
            this.webp = webp;
        }
    }
}
