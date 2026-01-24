package org.redlance.platformtools.impl.unsupported;

import com.sun.jna.Platform;
import org.jetbrains.annotations.NotNull;
import org.redlance.platformtools.PlatformFileReferer;
import org.redlance.platformtools.PlatformFinderFavorites;

import java.util.Collections;
import java.util.Set;

public final class UnsupportedPlatform implements PlatformFileReferer, PlatformFinderFavorites {
    private static final Set<String> UNSUPPORTED_STRING = Collections.singleton(
            String.format("Unsupported platform: %s", Platform.getOSType())
    );
    public static final UnsupportedPlatform INSTANCE = new UnsupportedPlatform();

    private UnsupportedPlatform() {}

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
}
