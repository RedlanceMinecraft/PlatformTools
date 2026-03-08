package org.redlance.platformtools.impl;

import com.sun.jna.Platform;
import org.jetbrains.annotations.NotNull;
import org.redlance.platformtools.PlatformProgressBars;
import org.redlance.platformtools.impl.macos.MacProgressBar;
import org.redlance.platformtools.impl.unsupported.UnsupportedPlatform;

public class PlatformProgressBarImpl implements PlatformProgressBars {
    private final @NotNull PlatformProgressBars nativeProgressBar = switch (Platform.getOSType()) {
        case Platform.MAC -> new MacProgressBar();
        default -> UnsupportedPlatform.INSTANCE;
    };

    @Override
    public PlatformProgressBar create() {
        return this.nativeProgressBar.create();
    }

    @Override
    public boolean isAvailable() {
        return this.nativeProgressBar.isAvailable();
    }
}
