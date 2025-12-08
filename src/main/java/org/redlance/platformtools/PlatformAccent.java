package org.redlance.platformtools;

import org.redlance.platformtools.impl.PlatformAccentImpl;

import java.awt.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public interface PlatformAccent {
    PlatformAccent INSTANCE = new PlatformAccentImpl();

    Color getAccent(Supplier<Color> fallback);

    void subscribeToChanges(Consumer<Color> consumer);
    boolean unsubscribeFromChanges(Consumer<Color> consumer);
}
