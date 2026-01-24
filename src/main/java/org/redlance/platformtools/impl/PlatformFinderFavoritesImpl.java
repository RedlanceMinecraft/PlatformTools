package org.redlance.platformtools.impl;

import com.sun.jna.Platform;
import org.redlance.platformtools.PlatformFinderFavorites;
import org.redlance.platformtools.impl.macos.MacFinderFavorites;
import org.redlance.platformtools.impl.unsupported.UnsupportedPlatform;

public class PlatformFinderFavoritesImpl implements PlatformFinderFavorites {
    private final PlatformFinderFavorites nativePlatformFinder = switch (Platform.getOSType()) {
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
}
