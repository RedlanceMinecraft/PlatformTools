package org.redlance.platformtools.webp;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.redlance.platformtools.webp.decoder.DecodedImage;
import org.redlance.platformtools.webp.decoder.PlatformWebPDecoder;
import org.redlance.platformtools.webp.impl.windows.WindowsCodecsDecoder;
import org.redlance.platformtools.webp.impl.windows.WindowsCodecsEncoder;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@EnabledOnOs(OS.WINDOWS)
class WindowsWICTest {
    @Test
    void decoderAvailable() {
        PlatformWebPDecoder dec = WindowsCodecsDecoder.tryCreate();
        assertNotNull(dec, "WIC decoder should be available on Windows");
        assertEquals("Windows WIC", dec.backendName());
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
        PlatformWebPDecoder dec = WindowsCodecsDecoder.tryCreate();
        assertNotNull(dec);

        DecodedImage decoded = dec.decode(TestUtils.loadWebP(name));
        assertTrue(decoded.width() > 0);
        assertTrue(decoded.height() > 0);
        assertEquals(decoded.width() * decoded.height(), decoded.argb().length);

        TestUtils.assertMatchesReference(decoded, name);
    }

    @Test
    void noEncoder() {
        assertNull(WindowsCodecsEncoder.tryCreate(), "WIC should not have WebP encoder");
    }
}
