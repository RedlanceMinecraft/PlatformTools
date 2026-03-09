package org.redlance.platformtools.webp;

import org.redlance.platformtools.webp.decoder.DecodedImage;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

final class TestUtils {
    static final int W = 64, H = 64;

    private TestUtils() {
    }

    static int[] generateTestImage() {
        int[] argb = new int[W * H];
        for (int y = 0; y < H; y++)
            for (int x = 0; x < W; x++)
                argb[y * W + x] = 0xFF000000 | ((x * 4) << 16) | ((y * 4) << 8) | 128;
        return argb;
    }

    static byte[] loadWebP(String name) throws IOException {
        try (var is = TestUtils.class.getResourceAsStream("/" + name + ".webp")) {
            assertNotNull(is, name + ".webp resource not found");
            return is.readAllBytes();
        }
    }

    static byte[] loadTestWebP() throws IOException {
        return loadWebP("test");
    }

    static DecodedImage loadReference(String name) throws IOException {
        try (var is = TestUtils.class.getResourceAsStream("/" + name + "_ref.png")) {
            assertNotNull(is, name + "_ref.png resource not found");
            return DecodedImage.fromPng(is.readAllBytes());
        }
    }

    /**
     * Compares decoded image against libwebp reference PNG (pixel-exact).
     * Skips pixels where both have alpha=0 (RGB undefined for transparent pixels).
     */
    static void assertMatchesReference(DecodedImage decoded, String name) throws IOException {
        DecodedImage ref = loadReference(name);
        assertEquals(ref.width(), decoded.width(), name + ": width mismatch vs reference");
        assertEquals(ref.height(), decoded.height(), name + ": height mismatch vs reference");

        int[] ep = ref.argb(), ap = decoded.argb();
        assertEquals(ep.length, ap.length, name + ": pixel count mismatch vs reference");
        for (int i = 0; i < ep.length; i++) {
            int e = ep[i], a = ap[i];
            if (e == a) continue;
            if ((e >>> 24) == 0 && (a >>> 24) == 0) continue;
            fail(name + " pixel [" + i + "] (" + (i % ref.width()) + "," + (i / ref.width())
                    + "): expected 0x" + Integer.toHexString(e)
                    + " but was 0x" + Integer.toHexString(a));
        }
    }
}
