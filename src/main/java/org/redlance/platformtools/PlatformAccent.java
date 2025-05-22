package org.redlance.platformtools;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.redlance.platformtools.impl.PlatformAccentImpl;

import java.awt.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public interface PlatformAccent {
    PlatformAccent INSTANCE = new PlatformAccentImpl();

    Color getAccent(Supplier<Color> fallback);

    @ApiStatus.NonExtendable
    default void subscribeToChanges(Consumer<Color> consumer) {
        subscribeToChanges(null, consumer);
    }

    void subscribeToChanges(@Nullable Long window, Consumer<Color> consumer);
}
