package org.redlance.platformtools.favorites.impl;

import com.sun.jna.Platform;
import org.jetbrains.annotations.NotNull;
import org.redlance.platformtools.favorites.PlatformFinderFavorites;
import org.redlance.platformtools.favorites.impl.macos.MacFinderFavorites;

public class PlatformFinderFavoritesImpl implements PlatformFinderFavorites {
    private final @NotNull PlatformFinderFavorites nativePlatformFinder = switch (Platform.getOSType()) {
        case Platform.MAC -> new MacFinderFavorites();
        default -> UnsupportedPlatform.INSTANCE;
    };

    @Override
    public boolean isPinned(String path) {
        return this.nativePlatformFinder.isPinned(path);
    }

    @Override
    public boolean pin(String path, boolean isFolder, Position position) {
        return this.nativePlatformFinder.pin(path, isFolder, position);
    }

    @Override
    public boolean unpin(String path) {
        return this.nativePlatformFinder.unpin(path);
    }

    @Override
    public boolean isAvailable() {
        return this.nativePlatformFinder.isAvailable();
    }

    private static final class UnsupportedPlatform implements PlatformFinderFavorites {
        static final UnsupportedPlatform INSTANCE = new UnsupportedPlatform();

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
        public boolean isAvailable() {
            return false;
        }
    }
}
