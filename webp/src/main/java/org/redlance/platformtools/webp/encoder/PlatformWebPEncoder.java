package org.redlance.platformtools.webp.encoder;

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
     * Encodes raw RGBA pixels into a lossless WebP image.
     *
     * @param rgba   pixel data in RGBA order, straight (non-premultiplied) alpha,
     *               length must be {@code width * height * 4}
     * @param width  image width in pixels
     * @param height image height in pixels
     * @return WebP file bytes
     * @throws IllegalStateException         if encoding fails
     * @throws UnsupportedOperationException if no backend is available
     */
    byte[] encodeLossless(byte[] rgba, int width, int height);

    /**
     * Encodes raw RGBA pixels into a lossy WebP image.
     *
     * @param rgba    pixel data in RGBA order, straight (non-premultiplied) alpha,
     *                length must be {@code width * height * 4}
     * @param width   image width in pixels
     * @param height  image height in pixels
     * @param quality compression quality, {@code 0.0f} (smallest) to {@code 1.0f} (best)
     * @return WebP file bytes
     * @throws IllegalStateException         if encoding fails
     * @throws UnsupportedOperationException if no backend is available
     */
    byte[] encodeLossy(byte[] rgba, int width, int height, float quality);

    boolean isAvailable();
}
