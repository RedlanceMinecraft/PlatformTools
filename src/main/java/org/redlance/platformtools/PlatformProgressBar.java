package org.redlance.platformtools;

import org.redlance.platformtools.impl.PlatformProgressBarImpl;

@SuppressWarnings("unused") // API
public interface PlatformProgressBar {
    PlatformProgressBar INSTANCE = new PlatformProgressBarImpl();

    PlatformProgressBar create(double maxValue);
    void incrementBy(double progress);
    void setMaxValue(double maxValue);
    void setValue(double value);
}
