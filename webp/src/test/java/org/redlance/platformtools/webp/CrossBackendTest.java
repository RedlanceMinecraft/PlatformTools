package org.redlance.platformtools.webp;

import org.junit.jupiter.api.Test;
import org.redlance.platformtools.webp.decoder.DecodedImage;
import org.redlance.platformtools.webp.decoder.PlatformWebPDecoder;
import org.redlance.platformtools.webp.impl.libwebp.LibWebPDecoder;
import org.redlance.platformtools.webp.impl.macos.MacOSImageIODecoder;
import org.redlance.platformtools.webp.impl.windows.WindowsCodecsDecoder;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class CrossBackendTest {
    @Test
    void allBackendsDecodeSamePixels() throws IOException {
        byte[] webp = TestUtils.loadTestWebP();

        PlatformWebPDecoder[] decoders = {
                LibWebPDecoder.tryCreate(),
                MacOSImageIODecoder.tryCreate(),
                WindowsCodecsDecoder.tryCreate()
        };

        DecodedImage reference = null;
        String referenceName = null;
        int tested = 0;

        for (PlatformWebPDecoder dec : decoders) {
            if (dec == null) continue;

            DecodedImage decoded = dec.decode(webp);
            assertTrue(decoded.width() > 0, "Invalid width: " + dec.backendName());
            assertTrue(decoded.height() > 0, "Invalid height: " + dec.backendName());
            assertEquals(decoded.width() * decoded.height(), decoded.argb().length,
                    "Pixel count mismatch: " + dec.backendName());

            if (reference == null) {
                reference = decoded;
                referenceName = dec.backendName();
            } else {
                assertEquals(reference.width(), decoded.width(),
                        "Width mismatch: " + referenceName + " vs " + dec.backendName());
                assertEquals(reference.height(), decoded.height(),
                        "Height mismatch: " + referenceName + " vs " + dec.backendName());
                assertPixelsEqual(reference.argb(), decoded.argb(),
                        referenceName + " vs " + dec.backendName());
            }
            tested++;
        }

        assertTrue(tested > 0, "At least one decoder should be available");
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
