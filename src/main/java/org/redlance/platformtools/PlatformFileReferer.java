package org.redlance.platformtools;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.redlance.platformtools.impl.PlatformFileRefererImpl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public interface PlatformFileReferer {
    PlatformFileReferer INSTANCE = new PlatformFileRefererImpl();

    @ApiStatus.NonExtendable
    default @Nullable String getFileReferer(File file) throws IOException {
        return getFileReferer(file.toPath());
    }

    @ApiStatus.NonExtendable
    default @Nullable String getFileReferer(Path path) throws IOException {
        return getFileReferer(path.toAbsolutePath().toString());
    }

    @Nullable String getFileReferer(String path) throws IOException;
}
