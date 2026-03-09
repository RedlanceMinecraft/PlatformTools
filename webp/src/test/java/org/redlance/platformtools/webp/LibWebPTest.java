package org.redlance.platformtools.webp;

import org.junit.jupiter.api.Test;
import org.redlance.platformtools.webp.decoder.DecodedImage;
import org.redlance.platformtools.webp.decoder.PlatformWebPDecoder;
import org.redlance.platformtools.webp.encoder.PlatformWebPEncoder;
import org.redlance.platformtools.webp.impl.libwebp.LibWebPDecoder;
import org.redlance.platformtools.webp.impl.libwebp.LibWebPEncoder;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class LibWebPTest {
    @Test
    void encoderAvailableWithDecoder() {
        PlatformWebPDecoder dec = LibWebPDecoder.tryCreate();
        assumeTrue(dec != null, "libwebp not available");
        assertNotNull(LibWebPEncoder.tryCreate(), "libwebp encoder should be available when decoder is");
    }

    @Test
    void losslessRoundtripExact() {
        PlatformWebPDecoder dec = LibWebPDecoder.tryCreate();
        PlatformWebPEncoder enc = LibWebPEncoder.tryCreate();
        assumeTrue(dec != null, "libwebp not available");

        int[] original = TestUtils.generateTestImage();
        byte[] encoded = enc.encodeLossless(original, TestUtils.W, TestUtils.H);
        DecodedImage decoded = dec.decode(encoded);
        assertArrayEquals(original, decoded.argb(), "Lossless roundtrip pixels must match exactly");
    }

    @Test
    void lossyRoundtripDimensions() {
        PlatformWebPDecoder dec = LibWebPDecoder.tryCreate();
        PlatformWebPEncoder enc = LibWebPEncoder.tryCreate();
        assumeTrue(dec != null, "libwebp not available");

        int[] original = TestUtils.generateTestImage();
        byte[] encoded = enc.encodeLossy(original, TestUtils.W, TestUtils.H, 0.75f);
        DecodedImage decoded = dec.decode(encoded);
        assertEquals(TestUtils.W, decoded.width());
        assertEquals(TestUtils.H, decoded.height());
        assertEquals(original.length, decoded.argb().length);
    }

    @Test
    void decodeTestFile() throws IOException {
        PlatformWebPDecoder dec = LibWebPDecoder.tryCreate();
        assumeTrue(dec != null, "libwebp not available");

        DecodedImage decoded = dec.decode(TestUtils.loadTestWebP());
        assertTrue(decoded.width() > 0);
        assertTrue(decoded.height() > 0);
        assertEquals(decoded.width() * decoded.height(), decoded.argb().length);
    }
}
