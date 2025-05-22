package org.redlance.platformtools.impl.macos;

import de.jangassen.jfa.JavaToObjc;
import de.jangassen.jfa.appkit.NSObject;
import de.jangassen.jfa.appkit.NSUserDefaults;
import de.jangassen.jfa.foundation.Foundation;
import org.jetbrains.annotations.Nullable;
import org.redlance.platformtools.PlatformAccent;
import org.redlance.platformtools.impl.macos.appkit.NSDistributedNotificationCenter;

import java.awt.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class MacAccent implements PlatformAccent {
    @Override
    public Color getAccent(Supplier<Color> fallback) {
        NSObject appleAccentColor = NSUserDefaults.standardUserDefaults().objectForKey("AppleAccentColor");
        if (appleAccentColor == null) return MacOSColors.ACCENT_BLUE;

        return switch (appleAccentColor.toString()) {
            case "-1" -> MacOSColors.ACCENT_GRAPHITE;
            case "0" -> MacOSColors.ACCENT_RED;
            case "1" -> MacOSColors.ACCENT_ORANGE;
            case "2" -> MacOSColors.ACCENT_YELLOW;
            case "3" -> MacOSColors.ACCENT_GREEN;
            case "5" -> MacOSColors.ACCENT_LILAC;
            case "6" -> MacOSColors.ACCENT_ROSE;
            default -> MacOSColors.ACCENT_BLUE;
        };
    }

    @Override
    public void subscribeToChanges(@Nullable Long window, Consumer<Color> consumer) {
        subscribeToNotificationChange(null, () -> {
            System.out.println("something");
        });

        subscribeToNotificationChange("AppleColorPreferencesChangedNotification", () -> {
            consumer.accept(getAccent(null));
            System.out.println("AppleColorPreferencesChangedNotification");
        });
        subscribeToNotificationChange("NSSystemColorsDidChangeNotification", () -> {
            System.out.println("NSSystemColorsDidChangeNotification");
        });
        subscribeToNotificationChange("AppleInterfaceThemeChangedNotification", () -> {
            System.out.println("AppleInterfaceThemeChangedNotification");
        });
        subscribeToNotificationChange("AccentChangedNotification", () -> {
            System.out.println("AccentChangedNotification");
        });
    }

    private static void subscribeToNotificationChange(String notify, Runnable runnable) {
        NSDistributedNotificationCenter.defaultCenter().addObserver(
                JavaToObjc.map(runnable, Runnable.class),
                Foundation.createSelector("run"),

                notify, null
        );
    }
}
