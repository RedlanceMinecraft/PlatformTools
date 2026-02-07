package org.redlance.platformtools.impl.macos;

import ca.weblite.objc.Client;
import ca.weblite.objc.NSObject;
import ca.weblite.objc.Proxy;
import ca.weblite.objc.RuntimeUtils;
import ca.weblite.objc.annotations.Msg;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.DoubleByReference;
import org.jetbrains.annotations.NotNull;
import org.redlance.platformtools.PlatformAccent;

import java.awt.Color;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class MacAccent implements PlatformAccent, Runnable {
    private static final String[] NOTIFICATION_NAMES = {
            "AppleColorPreferencesChangedNotification", // macOS 10.14+
            "AppleAquaColorVariantChanged", // macos 10.2+
    };

    protected static final Client CLIENT = Client.getInstance();

    private final List<Consumer<Color>> consumers = new CopyOnWriteArrayList<>();
    private final NotificationObserver observer = new NotificationObserver(this);

    private boolean isObserving = false;

    @Override
    public Color getAccent(@NotNull Supplier<Color> fallback) {
        Proxy rawColor = null;

        try { // macOS 10.14+
            rawColor = CLIENT.sendProxy("NSColor", "controlAccentColor");
        } catch (Throwable ignored) {}

        if (rawColor == null) {
            try { // macos 10.2+
                rawColor = CLIENT.sendProxy("NSColor", "keyboardFocusIndicatorColor");
            } catch (Throwable ignored) {
            }
        }

        if (rawColor == null) return fallback.get();

        Proxy colorSpace = CLIENT.sendProxy("NSColorSpace", "deviceRGBColorSpace");
        Proxy accentColor = rawColor.sendProxy("colorUsingColorSpace:", colorSpace);

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
    public void resubscribe() {
        if (this.isObserving || this.consumers.isEmpty()) return;
        Proxy center = CLIENT.sendProxy("NSDistributedNotificationCenter", "defaultCenter");
        Pointer observerPeer = observer.getPeer();
        Pointer selector = RuntimeUtils.sel("run");
        for (String notificationName : NOTIFICATION_NAMES) {
            center.send("addObserver:selector:name:object:", observerPeer, selector, notificationName, Pointer.NULL);
        }
        this.isObserving = true;
    }

    @Override
    public void subscribeToChanges(Consumer<Color> consumer) {
        this.consumers.add(consumer);
        resubscribe();
    }

    @Override
    public boolean unsubscribeFromChanges(Consumer<Color> consumer) {
        boolean removed = this.consumers.remove(consumer);
        if (this.isObserving && this.consumers.isEmpty()) {
            Proxy center = CLIENT.sendProxy("NSDistributedNotificationCenter", "defaultCenter");
            center.send("removeObserver:", observer.getPeer());
            this.isObserving = false;
        }
        return removed;
    }

    @Override
    public void run() {
        Color color = getAccent();
        for (Consumer<Color> consumer : this.consumers) {
            consumer.accept(color);
        }
    }

    @SuppressWarnings("unused")
    private static class NotificationObserver extends NSObject {
        private final Runnable runnable;

        public NotificationObserver(Runnable runnable) {
            super("NSObject");
            this.runnable = runnable;
        }

        @Msg(selector = "run", signature = "v@:")
        public void run() {
            this.runnable.run();
        }
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}
