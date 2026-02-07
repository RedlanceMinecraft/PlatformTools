package org.redlance.platformtools.impl.macos;

import ca.weblite.objc.Proxy;
import org.redlance.platformtools.PlatformProgressBars;
import org.redlance.platformtools.impl.macos.appkit.NSRect;

import java.util.ArrayList;
import java.util.List;

public final class MacProgressBar implements PlatformProgressBars {
    private static final int MAX_BARS = 8;
    private static final int SIZE = 128;
    private static final int MARGIN = 8;
    private static final int BAR_HEIGHT = 12;
    private static final int BAR_SPACING = 2;

    private final Proxy nsApp;
    private final Proxy dockTile;
    private final List<ProgressBar> bars = new ArrayList<>();
    private Proxy container;
    private Proxy imageView;

    public MacProgressBar() {
        this.nsApp = MacAccent.CLIENT.sendProxy("NSApplication", "sharedApplication");
        this.dockTile = this.nsApp.sendProxy("dockTile");
    }

    @Override
    public PlatformProgressBar create() throws TooManyProgressBarsException {
        if (this.bars.size() >= MAX_BARS) throw new TooManyProgressBarsException(MAX_BARS);

        if (this.container == null) {
            this.container = MacAccent.CLIENT.sendProxy("NSView", "alloc");
            this.container.send("initWithFrame:", new NSRect(0, 0, SIZE, SIZE));

            this.imageView = MacAccent.CLIENT.sendProxy("NSImageView", "alloc");
            this.imageView.send("initWithFrame:", new NSRect(0, 0, SIZE, SIZE));
            this.container.send("addSubview:", this.imageView);

            this.dockTile.send("setContentView:", this.container);
        }

        ProgressBar bar = new ProgressBar();
        this.bars.add(bar);
        this.container.send("addSubview:", bar.indicator);
        updateLayout();
        return bar;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    private void updateLayout() {
        int y = MARGIN;
        for (ProgressBar bar : this.bars) {
            bar.indicator.send("setFrame:", new NSRect(MARGIN, y, SIZE - MARGIN * 2, BAR_HEIGHT));
            y += BAR_HEIGHT + BAR_SPACING;
        }
        refresh();
    }

    private void refresh() {
        this.imageView.send("setImage:", this.nsApp.sendProxy("applicationIconImage"));
        this.dockTile.send("display");
    }

    private final class ProgressBar implements PlatformProgressBar {
        private final Proxy indicator;
        private boolean closed;

        ProgressBar() {
            this.indicator = MacAccent.CLIENT.sendProxy("NSProgressIndicator", "alloc");
            this.indicator.send("initWithFrame:", new NSRect(0, 0, 0, 0));
            this.indicator.send("setIndeterminate:", false);
            this.indicator.send("setMinValue:", 0.0);
        }

        @Override
        public void display() {
            if (this.closed) return;
            refresh();
        }

        @Override
        public void close() {
            if (this.closed) return;
            this.closed = true;

            this.indicator.send("removeFromSuperview");
            this.indicator.send("release");
            bars.remove(this);

            if (bars.isEmpty()) {
                dockTile.send("setContentView:", (Object) null);
                dockTile.send("display");
                imageView.send("release");
                container.send("release");
                imageView = null;
                container = null;
            } else {
                updateLayout();
            }
        }

        @Override
        public void incrementBy(double progress) {
            if (this.closed) return;
            this.indicator.send("incrementBy:", progress);
            refresh();
        }

        @Override
        public void setMaxValue(double maxValue) {
            if (this.closed) return;
            this.indicator.send("setMaxValue:", maxValue);
            refresh();
        }

        @Override
        public void setValue(double value) {
            if (this.closed) return;
            this.indicator.send("setDoubleValue:", value);
            refresh();
        }
    }
}
