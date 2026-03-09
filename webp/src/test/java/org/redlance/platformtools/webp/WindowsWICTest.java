package org.redlance.platformtools.webp;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
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

    @Test
    void decodeTestFile() throws IOException {
        PlatformWebPDecoder dec = WindowsCodecsDecoder.tryCreate();
        assertNotNull(dec);

        DecodedImage decoded = dec.decode(TestUtils.loadTestWebP());
        assertTrue(decoded.width() > 0);
        assertTrue(decoded.height() > 0);
        assertEquals(decoded.width() * decoded.height(), decoded.argb().length);
    }

    @Test
    void noEncoder() {
        assertNull(WindowsCodecsEncoder.tryCreate(), "WIC should not have WebP encoder");
    }
}
