package org.redlance.platformtools.referer.impl;

import com.sun.jna.Platform;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.redlance.platformtools.referer.PlatformFileReferer;
import org.redlance.platformtools.referer.impl.macos.MacFileReferer;
import org.redlance.platformtools.referer.impl.windows.WindowsFileReferer;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

public class PlatformFileRefererImpl implements PlatformFileReferer {
    private static final Set<String> UNSUPPORTED_STRING = Collections.singleton(
            String.format("Unsupported platform: %s", Platform.getOSType())
    );

    private final @Nullable PlatformFileReferer nativePlatformReferer = switch (Platform.getOSType()) {
        case Platform.WINDOWS -> new WindowsFileReferer();
        case Platform.MAC -> new MacFileReferer();
        default -> null;
    };

    @Override
    public @NotNull Set<String> getFileReferer(String path) throws IOException {
        if (this.nativePlatformReferer == null) return UNSUPPORTED_STRING;
        return this.nativePlatformReferer.getFileReferer(path);
    }

    @Override
    public boolean isAvailable() {
        return this.nativePlatformReferer != null && this.nativePlatformReferer.isAvailable();
    }
}
