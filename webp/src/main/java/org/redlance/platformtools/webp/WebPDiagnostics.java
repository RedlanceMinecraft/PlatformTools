package org.redlance.platformtools.webp;

import org.redlance.platformtools.webp.decoder.PlatformWebPDecoder;
import org.redlance.platformtools.webp.encoder.PlatformWebPEncoder;

import java.util.function.Consumer;

/**
 * Diagnostics utility for WebP backend status.
 *
 * <p>Reports which decoder/encoder backends are active and suggests
 * improvements based on the current platform and available libraries.
 */
@SuppressWarnings("unused") // API
public final class WebPDiagnostics {
    private WebPDiagnostics() {
    }

    /**
     * Sends a human-readable summary of the current WebP backend status
     * line-by-line to the given consumer.
     *
     * @param output line consumer (e.g. {@code logger::info})
     */
    public static void summary(Consumer<String> output) {
        output.accept("=== WebP Backend Status ===");

        appendDecoderStatus(output);
        appendEncoderStatus(output);
        appendSuggestions(output);
    }

    private static void appendDecoderStatus(Consumer<String> output) {
        PlatformWebPDecoder decoder = PlatformWebPDecoder.INSTANCE;
        output.accept("Decoder: " + (decoder.isAvailable() ? decoder.backendName() : "UNAVAILABLE"));
    }

    private static void appendEncoderStatus(Consumer<String> output) {
        PlatformWebPEncoder encoder = PlatformWebPEncoder.INSTANCE;
        output.accept("Encoder: " + (encoder.isAvailable() ? encoder.backendName() : "UNAVAILABLE"));
    }

    private static void appendSuggestions(Consumer<String> output) {
        PlatformWebPDecoder decoder = PlatformWebPDecoder.INSTANCE;
        PlatformWebPEncoder encoder = PlatformWebPEncoder.INSTANCE;

        boolean decoderNative = decoder.isAvailable() && "libwebp".equals(decoder.backendName());
        boolean encoderFull = encoder.isAvailable() && "libwebp".equals(encoder.backendName());

        if (decoderNative && encoderFull) {
            output.accept("Status: optimal (libwebp provides both encode and decode)");
            return;
        }

        output.accept("Suggestions:");
        String os = System.getProperty("os.name", "").toLowerCase();

        if (os.contains("mac")) {
            output.accept("- Install libwebp for full native encode/decode support: brew install webp");
        } else if (os.contains("linux")) {
            output.accept("- Install libwebp for full native encode/decode support:");
            output.accept("    apt install libwebp-dev  (Debian/Ubuntu)");
            output.accept("    dnf install libwebp-devel  (Fedora/RHEL)");
        } else if (os.contains("win")) {
            output.accept("- Install libwebp for full native encode/decode support:");
            output.accept("    Download from https://developers.google.com/speed/webp/download");
            output.accept("    and add the bin/ directory to PATH");
        }

        if (decoder.isAvailable() && !decoderNative) {
            String backend = decoder.backendName();
            if (backend.contains("ngengine")) {
                output.accept("- Decoder uses pure-Java fallback (slower than native libwebp)");
            } else if (backend.contains("macOS") || backend.contains("Windows")) {
                output.accept("- Decoder uses platform API (" + backend + "), libwebp may offer better performance and consistency");
            }
        }

        if (!decoder.isAvailable()) {
            output.accept("- No decoder available! Add the ngengine bundled dependency as a fallback");
        }

        if (encoder.isAvailable() && !encoderFull) {
            output.accept("- Encoder supports lossless mode only (bundled libwebp), install full libwebp for lossy encoding support");
        } else if (!encoder.isAvailable()) {
            output.accept("- No encoder available — libwebp is the only supported encoder backend");
        }
    }
}
