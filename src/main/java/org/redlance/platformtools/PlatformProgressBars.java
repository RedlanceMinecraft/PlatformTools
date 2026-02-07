package org.redlance.platformtools;

import org.redlance.platformtools.impl.PlatformProgressBarImpl;

import java.io.Closeable;

/**
 * Factory interface for creating platform-specific progress bar indicators.
 * <p>
 * This interface provides a platform-agnostic way to display progress bars
 * in native UI elements (such as the macOS dock icon).
 * <p>
 * Usage example:
 * <pre>{@code
 * PlatformProgressBars.PlatformProgressBar progressBar = PlatformProgressBars.INSTANCE.create();
 * progressBar.setMaxValue(100);
 * progressBar.display();
 * progressBar.setValue(50);
 * // ... when done
 * progressBar.close();
 * }</pre>
 *
 * @see PlatformProgressBar
 * @see BasePlatformFeature#isAvailable()
 */
@SuppressWarnings("unused") // API
public interface PlatformProgressBars extends BasePlatformFeature {
    /**
     * Singleton instance of the platform-specific progress bar factory.
     */
    PlatformProgressBars INSTANCE = new PlatformProgressBarImpl();

    /**
     * Creates a new platform-specific progress bar instance.
     * <p>
     * The returned progress bar is not visible until {@link PlatformProgressBar#display()} is called.
     *
     * @return a new {@link PlatformProgressBar} instance
     * @throws TooManyProgressBarsException if the maximum number of progress bars has been reached
     */
    PlatformProgressBar create() throws TooManyProgressBarsException;

    /**
     * Represents a platform-specific progress bar that can be displayed
     * in native UI elements.
     */
    interface PlatformProgressBar extends Closeable {
        /**
         * Displays the progress bar in the native UI element.
         * <p>
         * This method should be called after configuring the progress bar
         * with {@link #setMaxValue(double)} and optionally {@link #setValue(double)}.
         */
        void display();

        /**
         * Removes the progress bar from the native UI element.
         * <p>
         * After calling this method, the progress bar will no longer be visible.
         */
        @Override
        void close();

        /**
         * Increments the current progress value by the specified amount.
         *
         * @param progress the amount to add to the current value
         */
        void incrementBy(double progress);

        /**
         * Sets the maximum value of the progress bar.
         *
         * @param maxValue the maximum progress value
         */
        void setMaxValue(double maxValue);

        /**
         * Sets the current progress value.
         *
         * @param value the current progress value (should be between 0 and maxValue)
         */
        void setValue(double value);

        /**
         * Sets whether the progress bar is in indeterminate mode.
         * <p>
         * In indeterminate mode, the progress bar shows an animated state
         * indicating that work is in progress but the amount is unknown.
         *
         * @param indeterminate true for indeterminate mode, false for determinate
         */
        void setIndeterminate(boolean indeterminate);
    }

    /**
     * Thrown when attempting to create more progress bars than the platform supports.
     */
    class TooManyProgressBarsException extends RuntimeException {
        private final int maxAllowed;

        public TooManyProgressBarsException(int maxAllowed) {
            super("Too many progress bars. Maximum allowed: " + maxAllowed);
            this.maxAllowed = maxAllowed;
        }

        /**
         * Returns the maximum number of progress bars allowed on this platform.
         *
         * @return maximum allowed progress bars
         */
        public int getMaxAllowed() {
            return this.maxAllowed;
        }
    }
}
