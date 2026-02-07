package org.redlance.platformtools.impl.macos;

import ca.weblite.objc.Client;
import ca.weblite.objc.Proxy;
import org.redlance.platformtools.PlatformProgressBar;
import org.redlance.platformtools.impl.macos.appkit.NSRect;

public class MacProgressBar implements PlatformProgressBar {
    protected double value = 0.0;
    protected double maxValue = 0.0;

    protected NSRect progressbarPos = new NSRect(8, 8, 112, 12);
    protected NSRect progressbarPosHidden = new NSRect(0, 0, 0, 0);

    protected boolean isSetHidden = false;

    protected static final Client CLIENT = Client.getInstance();

    protected final Proxy nsApp = CLIENT.sendProxy("NSApplication", "sharedApplication");
    protected final Proxy dockTile = nsApp.sendProxy("dockTile");
    protected final Proxy progressIndicator = CLIENT.sendProxy("NSProgressIndicator", "alloc");

    @Override
    public PlatformProgressBar create(double maxValue) {
        Proxy sharedApp = CLIENT.sendProxy("NSApplication", "sharedApplication");
        Proxy iconImage = sharedApp.sendProxy("applicationIconImage");

        Proxy container = CLIENT.sendProxy("NSView", "alloc");
        container.send("initWithFrame:", new NSRect(0, 0, 128, 128));

        Proxy imageView = CLIENT.sendProxy("NSImageView", "alloc");

        imageView.send("initWithFrame:", new NSRect(0, 0, 128, 128));
        imageView.send("setImage:", iconImage);

        progressIndicator.send("initWithFrame:", progressbarPos);
        progressIndicator.send("setIndeterminate:", false);
        progressIndicator.send("setControlSize:", 1);
        progressIndicator.send("setAutoresizingMask:", 0);
        progressIndicator.send("setWantsLayer:", true);
        progressIndicator.send("setMinValue:", 0.0);
        setMaxValue(maxValue);
        setValue(0.0);
        progressIndicator.send("setStyle:", 0);
        progressIndicator.send("setWantsLayer:", true);

        imageView.send("addSubview:", progressIndicator);

        container.send("addSubview:", imageView);
        container.send("addSubview:", progressIndicator);

        dockTile.send("setContentView:", container);

        dockTile.send("display");

        return this;
    }

    @Override
    public void incrementBy(double progress) {
        this.value += progress;
        progressIndicator.send("setDoubleValue:", this.value);
        dockTile.send("display");
    }

    @Override
    public void setMaxValue(double maxValue) {
        this.maxValue = maxValue;
        progressIndicator.send("setMaxValue:", this.maxValue);
        dockTile.send("display");
    }

    @Override
    public void setValue(double value) {
        this.value = value;
        progressIndicator.send("setDoubleValue:", this.value);

        if (value >= maxValue) {
            this.isSetHidden = true;
            progressIndicator.send("setFrame:", progressbarPosHidden);
        } else if (isSetHidden) {
            progressIndicator.send("setFrame:", progressbarPos);
        }

        dockTile.send("display");
    }
}
