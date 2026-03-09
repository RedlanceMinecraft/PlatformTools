package org.redlance.platformtools.webp;

import org.junit.jupiter.api.Test;
import org.redlance.platformtools.webp.decoder.PlatformWebPDecoder;
import org.redlance.platformtools.webp.encoder.PlatformWebPEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

class AutoBackendTest {
    @Test
    void autoDecoderBackend() {
        String expected = System.getProperty("expected.decode", "");
        assumeFalse(expected.isEmpty(), "No expected decoder specified");
        assertTrue(PlatformWebPDecoder.INSTANCE.isAvailable(), "Decoder should be available");
        assertEquals(expected, PlatformWebPDecoder.INSTANCE.backendName());
    }

    @Test
    void autoEncoderBackend() {
        String expected = System.getProperty("expected.encode", "");
        assumeFalse(expected.isEmpty(), "No expected encoder specified");
        assertTrue(PlatformWebPEncoder.INSTANCE.isAvailable(), "Encoder should be available");
        assertEquals(expected, PlatformWebPEncoder.INSTANCE.backendName());
    }
}
