package org.redlance.platformtools.impl;

import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import org.redlance.platformtools.PlatformAccent;
import org.redlance.platformtools.impl.macos.MacAccent;
import org.redlance.platformtools.impl.windows.WindowsAccent;

import java.awt.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class PlatformAccentImpl implements PlatformAccent {
    private final PlatformAccent nativePlatformAccent = switch (Platform.getOSType()) {
        case Platform.WINDOWS -> new WindowsAccent();
        case Platform.MAC -> new MacAccent();
        default -> null;
    };

    @Override
    public Color getAccent(Supplier<Color> fallback) {
        if (this.nativePlatformAccent == null) return fallback.get();
        return this.nativePlatformAccent.getAccent(fallback);
    }

    @Override
    public void subscribeToChanges(Consumer<Color> consumer) {
        if (this.nativePlatformAccent == null) return;
        this.nativePlatformAccent.subscribeToChanges(consumer);
    }

    @Override
    public boolean unsubscribeFromChanges(Consumer<Color> consumer) {
        if (this.nativePlatformAccent == null) return false;
        return this.nativePlatformAccent.unsubscribeFromChanges(consumer);
    }
}
