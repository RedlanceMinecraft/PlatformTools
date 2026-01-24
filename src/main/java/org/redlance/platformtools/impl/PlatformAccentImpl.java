package org.redlance.platformtools.impl;

import com.sun.jna.Platform;
import org.jetbrains.annotations.NotNull;
import org.redlance.platformtools.PlatformAccent;
import org.redlance.platformtools.impl.macos.MacAccent;
import org.redlance.platformtools.impl.unsupported.UnsupportedPlatform;
import org.redlance.platformtools.impl.windows.WindowsAccent;

import java.awt.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class PlatformAccentImpl implements PlatformAccent {
    private final @NotNull PlatformAccent nativePlatformAccent = switch (Platform.getOSType()) {
        case Platform.WINDOWS -> new WindowsAccent();
        case Platform.MAC -> new MacAccent();
        default -> UnsupportedPlatform.INSTANCE;
    };

    @Override
    public Color getAccent(@NotNull Supplier<Color> fallback) {
        return this.nativePlatformAccent.getAccent(fallback);
    }

    @Override
    public void resubscribe() {
        this.nativePlatformAccent.resubscribe();
    }

    @Override
    public void subscribeToChanges(Consumer<Color> consumer) {
        this.nativePlatformAccent.subscribeToChanges(consumer);
    }

    @Override
    public boolean unsubscribeFromChanges(Consumer<Color> consumer) {
        return this.nativePlatformAccent.unsubscribeFromChanges(consumer);
    }
}
