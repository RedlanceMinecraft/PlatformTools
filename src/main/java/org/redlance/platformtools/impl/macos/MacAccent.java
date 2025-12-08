package org.redlance.platformtools.impl.macos;

import ca.weblite.objc.Client;
import ca.weblite.objc.NSObject;
import ca.weblite.objc.Proxy;
import ca.weblite.objc.RuntimeUtils;
import ca.weblite.objc.annotations.Msg;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.DoubleByReference;
import org.redlance.platformtools.PlatformAccent;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class MacAccent implements PlatformAccent, Runnable {
    protected static final Client CLIENT = Client.getInstance();

    private final List<Consumer<Color>> consumers = new ArrayList<>();

    public MacAccent() {
        subscribeToNotificationChange("AppleColorPreferencesChangedNotification", this);
    }

    @Override
    public Color getAccent(Supplier<Color> fallback) {
        Proxy colorSpace = CLIENT.sendProxy("NSColorSpace", "deviceRGBColorSpace");

        Proxy accentColor = CLIENT.sendProxy("NSColor", "controlAccentColor")
                .sendProxy("colorUsingColorSpace:", colorSpace);

        DoubleByReference red = new DoubleByReference();
        DoubleByReference green = new DoubleByReference();
        DoubleByReference blue = new DoubleByReference();
        DoubleByReference alpha = new DoubleByReference();
        accentColor.send("getRed:green:blue:alpha:", red, green, blue, alpha);

        float rd = (float) red.getValue();
        float gd = (float) green.getValue();
        float bd = (float) blue.getValue();
        float ad = (float) alpha.getValue();
        return new Color(rd, gd, bd, ad);
    }

    @Override
    public void subscribeToChanges(Pointer window, Consumer<Color> consumer) {
        this.consumers.add(consumer);
    }

    @Override
    public boolean unsubscribeFromChanges(Consumer<Color> consumer) {
        return this.consumers.remove(consumer);
    }

    protected static void subscribeToNotificationChange(String notify, Runnable runnable) {
        NotificationObserver observer = new NotificationObserver(runnable);
        Proxy center = CLIENT.sendProxy("NSDistributedNotificationCenter", "defaultCenter");
        center.send("addObserver:selector:name:object:", observer.getPeer(), RuntimeUtils.sel("run"), notify, Pointer.NULL);
    }

    @Override
    public void run() {
        Color color = getAccent(null);
        for (Consumer<Color> consumer : this.consumers) {
            consumer.accept(color);
        }
    }

    @SuppressWarnings("unused")
    private static class NotificationObserver extends NSObject {
        private final Runnable runnable;

        public NotificationObserver(Runnable runnable) {
            super();
            this.runnable = runnable;
            init("NSObject");
        }

        @Msg(selector = "run", signature = "v@:")
        public void run() {
            this.runnable.run();
        }
    }
}
