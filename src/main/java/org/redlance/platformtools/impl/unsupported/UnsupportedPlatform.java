package org.redlance.platformtools.impl.unsupported;

import com.sun.jna.Platform;
import org.redlance.platformtools.PlatformFileReferer;

public final class UnsupportedPlatform implements PlatformFileReferer {
    private static final String UNSUPPORTED_STRING = String.format("Unsupported platform: %s", Platform.getOSType());
    public static final UnsupportedPlatform INSTANCE = new UnsupportedPlatform();

    private UnsupportedPlatform() {}

    @Override
    public String getFileReferer(String path) {
        return UNSUPPORTED_STRING;
    }
}
