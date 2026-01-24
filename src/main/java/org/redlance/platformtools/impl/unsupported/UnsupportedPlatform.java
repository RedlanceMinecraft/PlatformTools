package org.redlance.platformtools.impl.unsupported;

import com.sun.jna.Platform;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.redlance.platformtools.PlatformAccent;
import org.redlance.platformtools.PlatformFileReferer;
import org.redlance.platformtools.PlatformFinderFavorites;

import java.awt.Color;
import java.util.Collections;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class UnsupportedPlatform implements PlatformAccent, PlatformFileReferer, PlatformFinderFavorites {
    private static final Set<String> UNSUPPORTED_STRING = Collections.singleton(
            String.format("Unsupported platform: %s", Platform.getOSType())
    );
    public static final UnsupportedPlatform INSTANCE = new UnsupportedPlatform();

    private UnsupportedPlatform() {}

    @Override
    public Color getAccent(@Nullable Supplier<Color> fallback) {
        return fallback == null ? MACOS_DEFAULT_ACCENT : fallback.get();
    }

    @Override
    public @NotNull Set<String> getFileReferer(String path) {
        return UNSUPPORTED_STRING;
    }

    @Override
    public boolean isPinned(String path) {
        return false;
    }

    @Override
    public boolean pin(String path, boolean isFolder, Position position) {
        return false;
    }

    @Override
    public boolean unpin(String path) {
        return false;
    }

    @Override
    public void resubscribe() {
        // no-op
    }

    @Override
    public void subscribeToChanges(Consumer<Color> consumer) {
        // no-op
    }

    @Override
    public boolean unsubscribeFromChanges(Consumer<Color> consumer) {
        return false;
    }
}
