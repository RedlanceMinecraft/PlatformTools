package org.redlance.platformtools.progress.impl.toolkit;

import org.redlance.platformtools.progress.PlatformProgressBars;

import java.awt.Taskbar;

public final class ToolkitProgressBar implements PlatformProgressBars {
    private static final int MAX_BARS = 1;

    private final Taskbar taskbar = Taskbar.getTaskbar();
    private ProgressBar activeBar;

    @Override
    public PlatformProgressBar create() throws TooManyProgressBarsException {
        if (this.activeBar != null) throw new TooManyProgressBarsException(MAX_BARS);

        this.activeBar = new ProgressBar();
        return this.activeBar;
    }

    @Override
    public int getMaxBars() {
        return MAX_BARS;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    private void refresh() {
        if (this.activeBar == null) {
            this.taskbar.setProgressValue(-1);
            return;
        }

        if (this.activeBar.maxValue > 0) {
            this.taskbar.setProgressValue((int) (this.activeBar.value / this.activeBar.maxValue * 100));
        } else {
            this.taskbar.setProgressValue(0);
        }
    }

    private final class ProgressBar implements PlatformProgressBar {
        private double value;
        private double maxValue = 100;
        private boolean closed;

        @Override
        public void display() {
            if (this.closed) return;
            refresh();
        }

        @Override
        public void close() {
            if (this.closed) return;
            this.closed = true;

            activeBar = null;
            refresh();
        }

        @Override
        public void incrementBy(double progress) {
            if (this.closed) return;
            this.value += progress;
            refresh();
        }

        @Override
        public void setMaxValue(double maxValue) {
            if (this.closed) return;
            this.maxValue = maxValue;
            refresh();
        }

        @Override
        public void setValue(double value) {
            if (this.closed) return;
            this.value = value;
            refresh();
        }

        @Override
        public void setIndeterminate(boolean indeterminate) {
            if (this.closed) return;
            refresh();
        }
    }
}
