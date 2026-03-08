package org.redlance.platformtools.accent.impl;

import com.sun.jna.Platform;
import org.jetbrains.annotations.NotNull;
import org.redlance.platformtools.accent.PlatformAccent;
import org.redlance.platformtools.accent.impl.macos.MacAccent;
import org.redlance.platformtools.accent.impl.windows.WindowsAccent;

import java.awt.Color;
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

    @Override
    public boolean isAvailable() {
        return this.nativePlatformAccent.isAvailable();
    }

    private static final class UnsupportedPlatform implements PlatformAccent {
        static final UnsupportedPlatform INSTANCE = new UnsupportedPlatform();

        @Override
        public Color getAccent(@NotNull Supplier<Color> fallback) {
            return fallback.get();
        }

        @Override
        public void resubscribe() {
        }

        @Override
        public void subscribeToChanges(Consumer<Color> consumer) {
        }

        @Override
        public boolean unsubscribeFromChanges(Consumer<Color> consumer) {
            return false;
        }

        @Override
        public boolean isAvailable() {
            return false;
        }
    }
}
