package org.redlance.platformtools.progress.impl;

import com.sun.jna.Platform;
import org.jetbrains.annotations.NotNull;
import org.redlance.platformtools.progress.PlatformProgressBars;
import org.redlance.platformtools.progress.impl.macos.MacProgressBar;

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

    private static final class UnsupportedPlatform implements PlatformProgressBars, PlatformProgressBar {
        static final UnsupportedPlatform INSTANCE = new UnsupportedPlatform();
        @Override public PlatformProgressBar create() { return this; }
        @Override public boolean isAvailable() { return false; }
        @Override public void display() {}
        @Override public void close() {}
        @Override public void incrementBy(double progress) {}
        @Override public void setMaxValue(double maxValue) {}
        @Override public void setValue(double value) {}
        @Override public void setIndeterminate(boolean indeterminate) {}
    }
}
