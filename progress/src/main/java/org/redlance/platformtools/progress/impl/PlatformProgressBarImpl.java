package org.redlance.platformtools.progress.impl;

import com.sun.jna.Platform;
import org.jetbrains.annotations.Nullable;
import org.redlance.platformtools.progress.PlatformProgressBars;
import org.redlance.platformtools.progress.impl.macos.MacProgressBar;
import org.redlance.platformtools.progress.impl.toolkit.ToolkitProgressBar;

import java.awt.Taskbar;

public class PlatformProgressBarImpl implements PlatformProgressBars {
    private final @Nullable PlatformProgressBars nativeProgressBar = switch (Platform.getOSType()) {
        case Platform.MAC -> new MacProgressBar();
        default -> createToolkitFallback();
    };

    @Override
    public PlatformProgressBar create() {
        if (this.nativeProgressBar == null) return UnsupportedProgressBar.INSTANCE;
        return this.nativeProgressBar.create();
    }

    @Override
    public int getMaxBars() {
        if (this.nativeProgressBar == null) return -1;
        return this.nativeProgressBar.getMaxBars();
    }

    @Override
    public boolean isAvailable() {
        return this.nativeProgressBar != null && this.nativeProgressBar.isAvailable();
    }

    private static @Nullable PlatformProgressBars createToolkitFallback() {
        try {
            if (Taskbar.isTaskbarSupported() && Taskbar.getTaskbar().isSupported(Taskbar.Feature.PROGRESS_VALUE)) {
                return new ToolkitProgressBar();
            }
        } catch (Throwable ignored) {
        }

        return null;
    }

    private static final class UnsupportedProgressBar implements PlatformProgressBar {
        private static final UnsupportedProgressBar INSTANCE = new UnsupportedProgressBar();

        @Override
        public void display() {}

        @Override
        public void close() {}

        @Override
        public void incrementBy(double progress) {}

        @Override
        public void setMaxValue(double maxValue) {}

        @Override
        public void setValue(double value) {}

        @Override
        public void setIndeterminate(boolean indeterminate) {}
    }
}
