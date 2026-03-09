package org.redlance.platformtools.webp.decoder;

import org.redlance.platformtools.webp.impl.PlatformWebPDecoderImpl;

/**
 * Platform-native WebP image decoder.
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
public interface PlatformWebPDecoder {
    PlatformWebPDecoder INSTANCE = new PlatformWebPDecoderImpl();

    /**
     * Returns the name of the active backend (e.g. "libwebp", "macOS ImageIO", "Windows WIC").
     *
     * @throws UnsupportedOperationException if no backend is available
     */
    String backendName();

    /**
     * Decodes a WebP image into packed ARGB pixels.
     *
     * @param webpData raw WebP file bytes
     * @return decoded image with ARGB pixel data
     * @throws IllegalStateException         if the data is invalid or decoding fails
     * @throws UnsupportedOperationException if no backend is available
     */
    DecodedImage decode(byte[] webpData);

    /**
     * Returns the dimensions of a WebP image without fully decoding it.
     *
     * @param webpData raw WebP file bytes
     * @return {@code int[]{width, height}}
     * @throws IllegalStateException         if the data is invalid
     * @throws UnsupportedOperationException if no backend is available
     */
    int[] getInfo(byte[] webpData);

    /**
     * Returns {@code true} if the given data is a valid WebP image.
     *
     * @param data raw bytes to check
     */
    default boolean isWebP(byte[] data) {
        if (data == null || data.length == 0) return false;

        try {
            int[] info = getInfo(data);
            return info[0] > 0 && info[1] > 0;
        } catch (Exception e) {
            return false;
        }
    }

    boolean isAvailable();
}
