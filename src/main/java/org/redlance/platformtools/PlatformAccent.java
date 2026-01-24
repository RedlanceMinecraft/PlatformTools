package org.redlance.platformtools;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.redlance.platformtools.impl.PlatformAccentImpl;

import java.awt.Color;
import java.util.function.Consumer;
import java.util.function.Supplier;

@SuppressWarnings("unused") // API
public interface PlatformAccent {
    Color MACOS_DEFAULT_ACCENT = new Color(0, 122, 255);
    PlatformAccent INSTANCE = new PlatformAccentImpl();

    /**
     * Retrieves the current system accent color.
     *
     * @return The current system accent color, or {@link PlatformAccent#MACOS_DEFAULT_ACCENT}
     */
    @ApiStatus.NonExtendable
    default Color getAccent() {
        return getAccent(() -> MACOS_DEFAULT_ACCENT);
    }

    /**
     * Retrieves the current system accent color.
     *
     * @param fallback A supplier that provides a default color if the system accent color cannot be determined.
     * @return The current system accent color, or the color provided by the fallback supplier.
     */
    Color getAccent(@NotNull Supplier<Color> fallback);

    /**
     * Re-subscribes the internal native listener to system events.
     * <p>
     * This method is useful for refreshing the native hook if the application window
     * has been recreated or the native connection was lost.
     */
    void resubscribe();

    /**
     * Subscribes a consumer to receive updates when the system accent color changes.
     *
     * @param consumer The consumer that will accept the new {@link Color} upon change.
     */
    void subscribeToChanges(Consumer<Color> consumer);

    /**
     * Unsubscribes a previously registered consumer from accent color updates.
     *
     * @param consumer The consumer to remove.
     * @return {@code true} if the consumer was successfully removed; {@code false} otherwise.
     */
    boolean unsubscribeFromChanges(Consumer<Color> consumer);
}
