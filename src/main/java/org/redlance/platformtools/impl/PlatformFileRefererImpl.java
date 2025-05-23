package org.redlance.platformtools.impl;

import com.sun.jna.Platform;
import org.jetbrains.annotations.Nullable;
import org.redlance.platformtools.PlatformFileReferer;
import org.redlance.platformtools.impl.macos.MacFileReferer;
import org.redlance.platformtools.impl.windows.WindowsFileReferer;

import java.io.IOException;

public class PlatformFileRefererImpl implements PlatformFileReferer {
    private final PlatformFileReferer nativePlatformReferer = switch (Platform.getOSType()) {
        case Platform.WINDOWS -> new WindowsFileReferer();
        case Platform.MAC -> new MacFileReferer();
        default -> null;
    };

    @Override
    public @Nullable String getFileReferer(String path) throws IOException {
        return this.nativePlatformReferer.getFileReferer(path);
    }
}
