package org.redlance.platformtools.impl;

import com.sun.jna.Platform;
import org.jetbrains.annotations.NotNull;
import org.redlance.platformtools.PlatformProgressBar;
import org.redlance.platformtools.impl.macos.MacProgressBar;
import org.redlance.platformtools.impl.unsupported.UnsupportedPlatform;

public class PlatformProgressBarImpl implements PlatformProgressBar {
    private final @NotNull PlatformProgressBar nativeProgressBar = switch (Platform.getOSType()) {
        //case Platform.WINDOWS -> new WindowsProgressBar();
        case Platform.MAC -> new MacProgressBar();
        default -> UnsupportedPlatform.INSTANCE;
    };

    @Override
    public PlatformProgressBar create(double maxValue) {
        return this.nativeProgressBar.create(maxValue);
    }

    @Override
    public void incrementBy(double progress) {
        this.nativeProgressBar.incrementBy(progress);
    }

    @Override
    public void setMaxValue(double maxValue) {
        this.nativeProgressBar.setMaxValue(maxValue);
    }

    @Override
    public void setValue(double value) {
        this.nativeProgressBar.setValue(value);
    }
}
