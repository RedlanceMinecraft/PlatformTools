package org.redlance.platformtools;

import org.jetbrains.annotations.ApiStatus;
import org.redlance.platformtools.impl.PlatformFinderFavoritesImpl;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

public interface PlatformFinderFavorites {
    PlatformFinderFavorites INSTANCE = new PlatformFinderFavoritesImpl();

    @ApiStatus.NonExtendable
    default boolean isPinned(File file) {
        return isPinned(file.toPath());
    }

    @ApiStatus.NonExtendable
    default boolean isPinned(Path path) {
        return isPinned(path.toAbsolutePath().toString());
    }

    boolean isPinned(String path);

    @ApiStatus.NonExtendable
    default boolean pin(File file, Position position) {
        return pin(file.toPath(), position);
    }

    @ApiStatus.NonExtendable
    default boolean pin(Path path, Position position) {
        return pin(path.toAbsolutePath().toString(), Files.isDirectory(path), position);
    }

    boolean pin(String path, boolean isFolder, Position position);

    enum Position {
        FIRST("kLSSharedFileListItemBeforeFirst"),
        LAST("kLSSharedFileListItemLast");

        public final String macOS;

        Position(String macOS) {
            this.macOS = macOS;
        }
    }

    @ApiStatus.NonExtendable
    default boolean unpin(File file) {
        return unpin(file.toPath());
    }

    @ApiStatus.NonExtendable
    default boolean unpin(Path path) {
        return unpin(path.toAbsolutePath().toString());
    }

    boolean unpin(String path);
}
