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
    void equalByPixels() throws IOException {
        DecodedImage image = DecodedImage.ofArgb(new int[]{1, 2, 3, 4}, 2, 2);
        DecodedImage copy = DecodedImage.ofArgb(new int[]{1, 2, 3, 4}, 2, 2);
        assertEquals(image, copy);
        assertEquals(image.hashCode(), copy.hashCode());
        assertNotEquals(image, DecodedImage.ofArgb(new int[]{1, 2, 3, 5}, 2, 2));
        assertNotEquals(image, DecodedImage.ofArgb(new int[]{1, 2, 3, 4}, 4, 1));

        byte[] webp = TestUtils.loadTestWebP();
        assertEquals(PlatformWebPDecoder.INSTANCE.decode(webp), DecodedImage.fromWebP(webp), "a lazy image should equal the decoded one");
    }

    @Test
    void isWebPDetection() throws IOException {
        assertTrue(PlatformWebPDecoder.INSTANCE.isWebP(TestUtils.loadTestWebP()));
        assertFalse(PlatformWebPDecoder.INSTANCE.isWebP(new byte[]{1, 2, 3}));
        assertFalse(PlatformWebPDecoder.INSTANCE.isWebP(new byte[0]));
        assertFalse(PlatformWebPDecoder.INSTANCE.isWebP(null));
    }
}
