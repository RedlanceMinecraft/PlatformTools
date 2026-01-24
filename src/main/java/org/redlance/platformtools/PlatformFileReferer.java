package org.redlance.platformtools;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.redlance.platformtools.impl.PlatformFileRefererImpl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

/**
 * Provides functionality to retrieve metadata regarding the origin or source (referrer) of a file.
 * <p>
 * On macOS, this typically corresponds to the "Where from" metadata attribute (kMDItemWhereFroms),
 * which stores the URLs from which a file was downloaded.
 */
@SuppressWarnings("unused") // API
public interface PlatformFileReferer {
    PlatformFileReferer INSTANCE = new PlatformFileRefererImpl();

    /**
     * Retrieves the set of referrers (source URLs) for the specified file.
     *
     * @param file The file to examine.
     * @return A non-null set of referrer strings. Returns an empty set if no referrers are found.
     * @throws IOException If an I/O error occurs while reading the file attributes.
     */
    @ApiStatus.NonExtendable
    default @NotNull Set<String> getFileReferer(File file) throws IOException {
        return getFileReferer(file.toPath());
    }

    /**
     * Retrieves the set of referrers (source URLs) for the file at the specified path.
     *
     * @param path The path to the file.
     * @return A non-null set of referrer strings. Returns an empty set if no referrers are found.
     * @throws IOException If an I/O error occurs while reading the file attributes.
     */
    @ApiStatus.NonExtendable
    default @NotNull Set<String> getFileReferer(Path path) throws IOException {
        return getFileReferer(path.toAbsolutePath().toString());
    }

    /**
     * Retrieves the set of referrers (source URLs) for the file at the specified string path.
     *
     * @param path The absolute path to the file.
     * @return A non-null set of referrer strings. Returns an empty set if no referrers are found.
     * @throws IOException If an I/O error occurs while reading the file attributes.
     */
    @NotNull Set<String> getFileReferer(String path) throws IOException;
}
