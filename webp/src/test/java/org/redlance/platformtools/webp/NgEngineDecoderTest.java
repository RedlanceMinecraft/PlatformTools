package org.redlance.platformtools.webp;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.redlance.platformtools.webp.decoder.DecodedImage;
import org.redlance.platformtools.webp.impl.ngengine.NgEngineDecoder;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class NgEngineDecoderTest {
    @Test
    void decoderAvailable() {
        NgEngineDecoder dec = NgEngineDecoder.tryCreate();
        assumeTrue(dec != null, "No Java WebP decoder on classpath");
        assertEquals("Java (ngengine)", dec.backendName());
    }

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
    void pixelExactDecode(String name) throws IOException {
        NgEngineDecoder dec = NgEngineDecoder.tryCreate();
        assumeTrue(dec != null, "No Java WebP decoder on classpath");

        DecodedImage decoded = dec.decode(TestUtils.loadWebP(name));
        assertTrue(decoded.width() > 0);
        assertTrue(decoded.height() > 0);
        assertEquals(decoded.width() * decoded.height(), decoded.argb().length);

        TestUtils.assertMatchesReference(decoded, name);
    }
}
