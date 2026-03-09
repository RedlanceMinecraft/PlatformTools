package org.redlance.platformtools;

/**
 * Base interface for all platform-specific features.
 *
 * <p>Implementations detect at construction time whether the required
 * native backend is present. Use {@link #isAvailable()} to check
 * before calling feature-specific methods.
 */
public interface BasePlatformFeature {
    /**
     * Returns {@code true} if the native backend was loaded successfully
     * and this feature can be used on the current platform.
     */
    boolean isAvailable();
}
