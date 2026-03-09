package org.redlance.platformtools.webp;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;

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

    static byte[] loadTestWebP() throws IOException {
        try (var is = TestUtils.class.getResourceAsStream("/test.webp")) {
            assertNotNull(is, "test.webp resource not found");
            return is.readAllBytes();
        }
    }
}
