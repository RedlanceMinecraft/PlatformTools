package org.redlance.platformtools.impl.utils;

import com.sun.jna.Native;

import javax.swing.*;

public final class StubFrame extends JFrame {
    private static final StubFrame INSTANCE = new StubFrame();
    public static final long WINDOW_ID = Native.getWindowID(StubFrame.INSTANCE);

    private StubFrame() {
        setTitle("PlatformTools Internal");
        setSize(0, 0);
        setVisible(true);
        setVisible(false);
    }
}
