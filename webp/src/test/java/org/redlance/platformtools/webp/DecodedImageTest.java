package org.redlance.platformtools.webp;

import org.junit.jupiter.api.Test;
import org.redlance.platformtools.webp.decoder.DecodedImage;
import org.redlance.platformtools.webp.decoder.PlatformWebPDecoder;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class DecodedImageTest {
    @Test
    void pngCaching() throws IOException {
        DecodedImage decoded = PlatformWebPDecoder.INSTANCE.decode(TestUtils.loadTestWebP());
        assertFalse(decoded.isPngCached());

        byte[] png = decoded.toPng();
        assertTrue(png.length > 0);
        assertTrue(decoded.isPngCached());
        assertSame(png, decoded.toPng(), "toPng() should return cached instance");
    }

    @Test
    void fromPngPreservesPixels() throws IOException {
        DecodedImage original = PlatformWebPDecoder.INSTANCE.decode(TestUtils.loadTestWebP());
        byte[] png = original.toPng();

        DecodedImage fromPng = DecodedImage.fromPng(png);
        assertEquals(original.width(), fromPng.width());
        assertEquals(original.height(), fromPng.height());
        assertTrue(fromPng.isPngCached());
        assertArrayEquals(original.argb(), fromPng.argb());
    }

    @Test
    void isWebPDetection() throws IOException {
        assertTrue(PlatformWebPDecoder.INSTANCE.isWebP(TestUtils.loadTestWebP()));
        assertFalse(PlatformWebPDecoder.INSTANCE.isWebP(new byte[]{1, 2, 3}));
        assertFalse(PlatformWebPDecoder.INSTANCE.isWebP(new byte[0]));
        assertFalse(PlatformWebPDecoder.INSTANCE.isWebP(null));
    }
}
