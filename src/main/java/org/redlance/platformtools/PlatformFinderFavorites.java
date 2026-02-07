package org.redlance.platformtools;

import org.jetbrains.annotations.ApiStatus;
import org.redlance.platformtools.impl.PlatformFinderFavoritesImpl;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

@SuppressWarnings("unused") // API
public interface PlatformFinderFavorites extends BasePlatformFeature {
    PlatformFinderFavorites INSTANCE = new PlatformFinderFavoritesImpl();

    /**
     * Checks if the specified file is currently pinned in the favorites list.
     *
     * @param file The file to check.
     * @return {@code true} if the file is pinned; {@code false} otherwise.
     */
    @ApiStatus.NonExtendable
    default boolean isPinned(File file) {
        return isPinned(file.toPath());
    }

    /**
     * Checks if the specified path is currently pinned in the favorites list.
     *
     * @param path The path to check.
     * @return {@code true} if the path is pinned; {@code false} otherwise.
     */
    @ApiStatus.NonExtendable
    default boolean isPinned(Path path) {
        return isPinned(path.toAbsolutePath().toString());
    }

    /**
     * Checks if the specified path string is currently pinned in the favorites list.
     *
     * @param path The absolute string path to check.
     * @return {@code true} if the path is currently in the favorites list; {@code false} otherwise.
     */
    boolean isPinned(String path);

    /**
     * Pins the specified file to the favorites list.
     *
     * @param file     The file to pin.
     * @param position The position where the item should be placed.
     * @return {@code true} if the list was updated (item added); {@code false} if nothing changed.
     */
    @ApiStatus.NonExtendable
    default boolean pin(File file, Position position) {
        return pin(file.toPath(), position);
    }

    /**
     * Pins the specified path to the favorites list.
     *
     * @param path     The path to pin.
     * @param position The position where the item should be placed.
     * @return {@code true} if the list was updated (item added); {@code false} if nothing changed.
     */
    @ApiStatus.NonExtendable
    default boolean pin(Path path, Position position) {
        return pin(path.toAbsolutePath().toString(), Files.isDirectory(path), position);
    }

    /**
     * Pins the specified path string to the favorites list.
     *
     * @param path     The absolute string path to pin.
     * @param isFolder {@code true} if the path points to a directory.
     * @param position The position within the list (e.g., {@link Position#LAST}).
     * @return {@code true} if the item was successfully added and the list was updated;
     * {@code false} if the list remained unchanged (e.g., item already exists or operation failed).
     */
    boolean pin(String path, boolean isFolder, Position position);

    enum Position {
        FIRST("kLSSharedFileListItemBeforeFirst"),
        LAST("kLSSharedFileListItemLast");

        public final String macOS;

        Position(String macOS) {
            this.macOS = macOS;
        }
    }

    /**
     * Removes the specified file from the favorites list.
     *
     * @param file The file to unpin.
     * @return {@code true} if the list was updated (item removed); {@code false} if nothing changed.
     */
    @ApiStatus.NonExtendable
    default boolean unpin(File file) {
        return unpin(file.toPath());
    }

    /**
     * Removes the specified path from the favorites list.
     *
     * @param path The path to unpin.
     * @return {@code true} if the list was updated (item removed); {@code false} if nothing changed.
     */
    @ApiStatus.NonExtendable
    default boolean unpin(Path path) {
        return unpin(path.toAbsolutePath().toString());
    }

    /**
     * Removes the specified path string from the favorites list.
     *
     * @param path The absolute string path to remove.
     * @return {@code true} if the item was successfully removed and the list was updated;
     * {@code false} if the list remained unchanged (e.g., item was not found).
     */
    boolean unpin(String path);
}
