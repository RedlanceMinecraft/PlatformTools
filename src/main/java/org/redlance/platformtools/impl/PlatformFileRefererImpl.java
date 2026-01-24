package org.redlance.platformtools.impl;

import com.sun.jna.Platform;
import org.jetbrains.annotations.NotNull;
import org.redlance.platformtools.PlatformFileReferer;
import org.redlance.platformtools.impl.macos.MacFileReferer;
import org.redlance.platformtools.impl.unsupported.UnsupportedPlatform;
import org.redlance.platformtools.impl.windows.WindowsFileReferer;

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
}
