package org.redlance.platformtools.impl.utils;

import javax.swing.*;

public class StubFrame extends JFrame {
    public StubFrame() {
        setTitle("PlatformTools Internal");
        setSize(0, 0);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        setVisible(true);
        setVisible(false);
    }
}
