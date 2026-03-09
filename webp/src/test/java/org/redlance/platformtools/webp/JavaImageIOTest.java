package org.redlance.platformtools.webp;

import org.junit.jupiter.api.Test;
import org.redlance.platformtools.webp.decoder.DecodedImage;
import org.redlance.platformtools.webp.impl.imageio.JavaImageIODecoder;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class JavaImageIOTest {
    @Test
    void decoderAvailable() {
        JavaImageIODecoder dec = JavaImageIODecoder.tryCreate();
        assumeTrue(dec != null, "No WebP ImageIO plugin on classpath");
        assertEquals("Java ImageIO", dec.backendName());
    }

    @Test
    void decodeTestFile() throws IOException {
        JavaImageIODecoder dec = JavaImageIODecoder.tryCreate();
        assumeTrue(dec != null, "No WebP ImageIO plugin on classpath");

        DecodedImage decoded = dec.decode(TestUtils.loadTestWebP());
        assertTrue(decoded.width() > 0);
        assertTrue(decoded.height() > 0);
        assertEquals(decoded.width() * decoded.height(), decoded.argb().length);
    }
}
