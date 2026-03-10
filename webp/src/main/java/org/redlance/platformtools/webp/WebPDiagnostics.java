package org.redlance.platformtools.webp;

import org.redlance.platformtools.webp.decoder.PlatformWebPDecoder;
import org.redlance.platformtools.webp.encoder.PlatformWebPEncoder;

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
     * Returns a human-readable summary of the current WebP backend status,
     * including active backends and suggestions for improvement.
     */
    public static String summary() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== WebP Backend Status ===\n");

        appendDecoderStatus(sb);
        sb.append('\n');
        appendEncoderStatus(sb);
        sb.append('\n');
        appendSuggestions(sb);

        return sb.toString();
    }

    private static void appendDecoderStatus(StringBuilder sb) {
        PlatformWebPDecoder decoder = PlatformWebPDecoder.INSTANCE;
        sb.append("Decoder: ");
        if (decoder.isAvailable()) {
            sb.append(decoder.backendName());
        } else {
            sb.append("UNAVAILABLE");
        }
        sb.append('\n');
    }

    private static void appendEncoderStatus(StringBuilder sb) {
        PlatformWebPEncoder encoder = PlatformWebPEncoder.INSTANCE;
        sb.append("Encoder: ");
        if (encoder.isAvailable()) {
            sb.append(encoder.backendName());
        } else {
            sb.append("UNAVAILABLE");
        }
        sb.append('\n');
    }

    private static void appendSuggestions(StringBuilder sb) {
        PlatformWebPDecoder decoder = PlatformWebPDecoder.INSTANCE;
        PlatformWebPEncoder encoder = PlatformWebPEncoder.INSTANCE;

        boolean decoderNative = decoder.isAvailable() && "libwebp".equals(decoder.backendName());
        boolean encoderAvailable = encoder.isAvailable();

        if (decoderNative && encoderAvailable) {
            sb.append("Status: optimal (libwebp provides both encode and decode)\n");
            return;
        }

        sb.append("Suggestions:\n");
        String os = System.getProperty("os.name", "").toLowerCase();

        sb.append("- Install libwebp for native encode/decode support:\n");
        if (os.contains("mac")) {
            sb.append("    brew install webp\n");
        } else if (os.contains("linux")) {
            sb.append("    apt install libwebp-dev  (Debian/Ubuntu)\n");
            sb.append("    dnf install libwebp-devel  (Fedora/RHEL)\n");
        } else if (os.contains("win")) {
            sb.append("    Download from https://developers.google.com/speed/webp/download\n");
            sb.append("    and add the bin/ directory to PATH\n");
        }

        if (decoder.isAvailable() && !decoderNative) {
            String backend = decoder.backendName();
            if (backend.contains("ngengine")) {
                sb.append("- Decoder uses pure-Java fallback (slower than native libwebp)\n");
            } else if (backend.contains("macOS") || backend.contains("Windows")) {
                sb.append("- Decoder uses platform API (").append(backend).append(")\n");
                sb.append("  libwebp may offer better performance and consistency\n");
            }
        }

        if (!decoder.isAvailable()) {
            sb.append("- No decoder available! Add the ngengine bundled dependency as a fallback\n");
        }

        if (!encoderAvailable) {
            sb.append("- No encoder available — libwebp is the only supported encoder backend\n");
        }
    }
}