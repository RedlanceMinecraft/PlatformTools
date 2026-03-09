package org.redlance.platformtools.webp;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.redlance.platformtools.webp.decoder.DecodedImage;
import org.redlance.platformtools.webp.decoder.PlatformWebPDecoder;
import org.redlance.platformtools.webp.impl.macos.MacOSImageIODecoder;
import org.redlance.platformtools.webp.impl.macos.MacOSImageIOEncoder;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@EnabledOnOs(OS.MAC)
class MacOSImageIOTest {
    @Test
    void decoderAvailable() {
        PlatformWebPDecoder dec = MacOSImageIODecoder.tryCreate();
        assertNotNull(dec, "macOS ImageIO decoder should be available");
        assertEquals("macOS ImageIO", dec.backendName());
    }

    @Test
    void decodeTestFile() throws IOException {
        PlatformWebPDecoder dec = MacOSImageIODecoder.tryCreate();
        assertNotNull(dec);

        DecodedImage decoded = dec.decode(TestUtils.loadTestWebP());
        assertTrue(decoded.width() > 0);
        assertTrue(decoded.height() > 0);
        assertEquals(decoded.width() * decoded.height(), decoded.argb().length);
    }

    @Test
    void encoderMayBeUnavailable() {
        // macOS currently does not support WebP encoding via ImageIO
        MacOSImageIOEncoder enc = MacOSImageIOEncoder.tryCreate();
        if (enc != null) {
            byte[] encoded = enc.encodeLossy(TestUtils.generateTestImage(), TestUtils.W, TestUtils.H, 0.75f);
            assertTrue(encoded.length > 0);
        }
    }
}
