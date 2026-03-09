package org.redlance.platformtools.accent.impl;

import com.sun.jna.Platform;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.redlance.platformtools.accent.PlatformAccent;
import org.redlance.platformtools.accent.impl.macos.MacAccent;
import org.redlance.platformtools.accent.impl.windows.WindowsAccent;

import java.awt.Color;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class PlatformAccentImpl implements PlatformAccent {
    private final @Nullable PlatformAccent nativePlatformAccent = switch (Platform.getOSType()) {
        case Platform.WINDOWS -> new WindowsAccent();
        case Platform.MAC -> new MacAccent();
        default -> null;
    };

    @Override
    public Color getAccent(@NotNull Supplier<Color> fallback) {
        if (this.nativePlatformAccent == null) return fallback.get();
        return this.nativePlatformAccent.getAccent(fallback);
    }

    @Override
    public void resubscribe() {
        if (this.nativePlatformAccent == null) return;
        this.nativePlatformAccent.resubscribe();
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

    @Override
    public boolean isAvailable() {
        return this.nativePlatformAccent != null && this.nativePlatformAccent.isAvailable();
    }
}
