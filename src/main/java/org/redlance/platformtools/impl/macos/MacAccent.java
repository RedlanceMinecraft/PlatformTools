package org.redlance.platformtools.impl.macos;

import com.sun.jna.ptr.DoubleByReference;
import de.jangassen.jfa.JavaToObjc;
import de.jangassen.jfa.appkit.NSColor;
import de.jangassen.jfa.appkit.NSColorSpace;
import de.jangassen.jfa.appkit.NSDistributedNotificationCenter;
import de.jangassen.jfa.foundation.Foundation;
import org.jetbrains.annotations.Nullable;
import org.redlance.platformtools.PlatformAccent;

import java.awt.Color;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class MacAccent implements PlatformAccent {
    @Override
    public Color getAccent(Supplier<Color> fallback) {
        NSColor accentColor = NSColor.controlAccentColor().colorUsingColorSpace(NSColorSpace.deviceRGBColorSpace());

        DoubleByReference red = new DoubleByReference();
        DoubleByReference green = new DoubleByReference();
        DoubleByReference blue = new DoubleByReference();
        DoubleByReference alpha = new DoubleByReference();
        accentColor.getRed(red, green, blue, alpha);

        float rd = (float) red.getValue();
        float gd = (float) green.getValue();
        float bd = (float) blue.getValue();
        float ad = (float) alpha.getValue();
        return new Color(rd, gd, bd, ad);
    }

    @Override
    public void subscribeToChanges(@Nullable Long window, Consumer<Color> consumer) {
        subscribeToNotificationChange("AppleColorPreferencesChangedNotification",
                () -> consumer.accept(getAccent(null))
        );
    }

    protected static void subscribeToNotificationChange(String notify, Runnable runnable) {
        NSDistributedNotificationCenter.defaultCenter().addObserver(
                JavaToObjc.map(runnable, Runnable.class),
                Foundation.createSelector("run"),

                notify, null
        );
    }
}
