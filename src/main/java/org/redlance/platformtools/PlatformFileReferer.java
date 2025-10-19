package org.redlance.platformtools;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.redlance.platformtools.impl.PlatformFileRefererImpl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

public interface PlatformFileReferer {
    PlatformFileReferer INSTANCE = new PlatformFileRefererImpl();

    @ApiStatus.NonExtendable
    default @NotNull Set<String> getFileReferer(File file) throws IOException {
        return getFileReferer(file.toPath());
    }

    @ApiStatus.NonExtendable
    default @NotNull Set<String> getFileReferer(Path path) throws IOException {
        return getFileReferer(path.toAbsolutePath().toString());
    }

    @NotNull Set<String> getFileReferer(String path) throws IOException;
}
