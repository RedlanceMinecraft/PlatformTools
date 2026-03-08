package org.redlance.platformtools.referer.impl;

import com.sun.jna.Platform;
import org.jetbrains.annotations.NotNull;
import org.redlance.platformtools.referer.PlatformFileReferer;
import org.redlance.platformtools.referer.impl.macos.MacFileReferer;
import org.redlance.platformtools.referer.impl.windows.WindowsFileReferer;

import java.io.IOException;
import java.util.Set;

public class PlatformFileRefererImpl implements PlatformFileReferer {
    private final @NotNull PlatformFileReferer nativePlatformReferer = switch (Platform.getOSType()) {
        case Platform.WINDOWS -> new WindowsFileReferer();
        case Platform.MAC -> new MacFileReferer();
        default -> UnsupportedPlatform.INSTANCE;
    };

    @Override
    public @NotNull Set<String> getFileReferer(String path) throws IOException {
        return this.nativePlatformReferer.getFileReferer(path);
    }

    @Override
    public boolean isAvailable() {
        return this.nativePlatformReferer.isAvailable();
    }

    private static final class UnsupportedPlatform implements PlatformFileReferer {
        static final UnsupportedPlatform INSTANCE = new UnsupportedPlatform();

        @Override
        public @NotNull Set<String> getFileReferer(String path) {
            return Set.of(String.format("Unsupported platform: %s", Platform.getOSType()));
        }

        @Override
        public boolean isAvailable() {
            return false;
        }
    }
}
