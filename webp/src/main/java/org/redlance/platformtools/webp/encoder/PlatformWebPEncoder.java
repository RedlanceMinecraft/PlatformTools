package org.redlance.platformtools.webp.encoder;

import org.redlance.platformtools.webp.decoder.DecodedImage;
import org.redlance.platformtools.webp.impl.PlatformWebPEncoderImpl;

/**
 * Platform-native WebP image encoder.
 *
 * <p>Automatically selects the best available backend:
 * <ul>
 *     <li><b>macOS</b> — ImageIO (CoreGraphics)</li>
 *     <li><b>Windows</b> — WIC (Windows Imaging Component)</li>
 *     <li><b>Cross-platform</b> — libwebp (if found on library path)</li>
 * </ul>
 *
 * <p>All methods throw if no backend is available; check {@link #isAvailable()} first
 * or handle {@link UnsupportedOperationException}.
 */
@SuppressWarnings("unused") // API
public interface PlatformWebPEncoder {
    PlatformWebPEncoder INSTANCE = new PlatformWebPEncoderImpl();

    /**
     * Returns the name of the active backend (e.g. "libwebp", "macOS ImageIO", "Windows WIC").
     *
     * @throws UnsupportedOperationException if no backend is available
     */
    String backendName();

    /**
     * Encodes ARGB pixels into a lossless WebP image.
     *
     * @param argb   pixel data as packed ARGB integers,
     *               length must be {@code width * height}
     * @param width  image width in pixels
     * @param height image height in pixels
     * @return WebP file bytes
     * @throws IllegalStateException         if encoding fails
     * @throws UnsupportedOperationException if no backend is available
     */
    byte[] encodeLossless(int[] argb, int width, int height);

    /**
     * Encodes ARGB pixels into a lossy WebP image.
     *
     * @param argb    pixel data as packed ARGB integers,
     *                length must be {@code width * height}
     * @param width   image width in pixels
     * @param height  image height in pixels
     * @param quality compression quality, {@code 0.0f} (smallest) to {@code 1.0f} (best)
     * @return WebP file bytes
     * @throws IllegalStateException         if encoding fails
     * @throws UnsupportedOperationException if no backend is available
     */
    byte[] encodeLossy(int[] argb, int width, int height, float quality);

    /**
     * Encodes a {@link DecodedImage} into a lossless WebP image.
     *
     * @param image the decoded image to encode
     * @return WebP file bytes
     * @throws IllegalStateException         if encoding fails
     * @throws UnsupportedOperationException if no backend is available
     */
    default byte[] encodeLossless(DecodedImage image) {
        byte[] cached = image.toWebP(null);
        if (cached != null) return cached;
        byte[] encoded = encodeLossless(image.argb(), image.width(), image.height());
        image.cacheWebP(null, encoded);
        return encoded;
    }

    /**
     * Encodes a {@link DecodedImage} into a lossy WebP image.
     *
     * @param image   the decoded image to encode
     * @param quality compression quality, {@code 0.0f} (smallest) to {@code 1.0f} (best)
     * @return WebP file bytes
     * @throws IllegalStateException         if encoding fails
     * @throws UnsupportedOperationException if no backend is available
     */
    default byte[] encodeLossy(DecodedImage image, float quality) {
        byte[] cached = image.toWebP(quality);
        if (cached != null) return cached;
        byte[] encoded = encodeLossy(image.argb(), image.width(), image.height(), quality);
        image.cacheWebP(quality, encoded);
        return encoded;
    }

    boolean isAvailable();
}
