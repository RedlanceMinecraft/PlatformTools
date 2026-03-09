package org.redlance.platformtools.webp;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.redlance.platformtools.webp.decoder.DecodedImage;
import org.redlance.platformtools.webp.decoder.PlatformWebPDecoder;
import org.redlance.platformtools.webp.impl.libwebp.LibWebPDecoder;
import org.redlance.platformtools.webp.impl.macos.MacOSImageIODecoder;
import org.redlance.platformtools.webp.impl.ngengine.NgEngineDecoder;
import org.redlance.platformtools.webp.impl.windows.WindowsCodecsDecoder;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class CrossBackendTest {
    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {
            "test",             // original test image (with alpha)
            "gradient_rgb",     // horizontal RGB gradient, opaque
            "gradient_alpha",   // alpha gradient
            "checkerboard",     // high-frequency pattern
            "solid",            // solid color
            "circle_alpha",     // circular alpha gradient 128x128
            "tall_gradient",    // 32x256, tests vertical accumulation
            "wide_gradient",    // 256x32
            "noise",            // pseudo-random noise pattern
    })
    void allBackendsDecodeSamePixels(String name) throws IOException {
        byte[] webp = TestUtils.loadWebP(name);

        PlatformWebPDecoder[] decoders = {
                LibWebPDecoder.tryCreate(),
                MacOSImageIODecoder.tryCreate(),
                WindowsCodecsDecoder.tryCreate(),
                NgEngineDecoder.tryCreate(),
        };

        DecodedImage reference = null;
        String referenceName = null;
        int tested = 0;

        for (PlatformWebPDecoder dec : decoders) {
            if (dec == null) continue;

            DecodedImage decoded = dec.decode(webp);
            assertTrue(decoded.width() > 0, name + ": invalid width from " + dec.backendName());
            assertTrue(decoded.height() > 0, name + ": invalid height from " + dec.backendName());
            assertEquals(decoded.width() * decoded.height(), decoded.argb().length,
                    name + ": pixel count mismatch from " + dec.backendName());

            if (reference == null) {
                reference = decoded;
                referenceName = dec.backendName();
            } else {
                String label = name + ": " + referenceName + " vs " + dec.backendName();
                assertEquals(reference.width(), decoded.width(), label + " width");
                assertEquals(reference.height(), decoded.height(), label + " height");
                assertPixelsEqual(reference.argb(), decoded.argb(), label);
            }
            tested++;
        }

        assumeTrue(tested >= 2, "Need at least 2 backends to cross-check");
    }

    // RGB is undefined when alpha=0 — skip those pixels
    private static void assertPixelsEqual(int[] expected, int[] actual, String label) {
        assertEquals(expected.length, actual.length, "Pixel count mismatch: " + label);
        for (int i = 0; i < expected.length; i++) {
            int e = expected[i], a = actual[i];
            if (e == a) continue;
            if ((e >>> 24) == 0 && (a >>> 24) == 0) continue;
            fail("Pixel [" + i + "]: expected 0x" + Integer.toHexString(e)
                    + " but was 0x" + Integer.toHexString(a) + " (" + label + ")");
        }
    }
}
