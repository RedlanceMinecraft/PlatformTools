package org.redlance.platformtools.favorites.impl;

import com.sun.jna.Platform;
import org.jetbrains.annotations.Nullable;
import org.redlance.platformtools.favorites.PlatformFinderFavorites;
import org.redlance.platformtools.favorites.impl.macos.MacFinderFavorites;

public class PlatformFinderFavoritesImpl implements PlatformFinderFavorites {
    private final @Nullable PlatformFinderFavorites nativePlatformFinder = switch (Platform.getOSType()) {
        case Platform.MAC -> new MacFinderFavorites();
        default -> null;
    };

    @Override
    public boolean isPinned(String path) {
        return this.nativePlatformFinder != null && this.nativePlatformFinder.isPinned(path);
    }

    @Override
    public boolean pin(String path, boolean isFolder, Position position) {
        return this.nativePlatformFinder != null && this.nativePlatformFinder.pin(path, isFolder, position);
    }

    @Override
    public boolean unpin(String path) {
        return this.nativePlatformFinder != null && this.nativePlatformFinder.unpin(path);
    }

    @Override
    public boolean isAvailable() {
        return this.nativePlatformFinder != null && this.nativePlatformFinder.isAvailable();
    }
}
