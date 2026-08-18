package org.redlance.platformtools.accent.impl.linux;

import org.jetbrains.annotations.Nullable;

import java.awt.Color;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

interface LinuxAccentSource {
    @Nullable Color readAccent();

    void listen(BooleanSupplier active, Consumer<Color> consumer);
}
