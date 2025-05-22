package org.redlance.platformtools.impl;

import org.redlance.platformtools.impl.utils.StubFrame;

import javax.swing.*;
import java.awt.*;

public class TestingApp extends StubFrame {
    private final JPanel colorPanel;

    public TestingApp(Color initialColor) {
        setTitle("Color Window");
        setSize(400, 300);

        this.colorPanel = new JPanel();
        this.colorPanel.setBackground(initialColor);
        add(this.colorPanel);

        setVisible(true);
    }

    public static void main(String[] args) {
        TestingApp testingApp = new TestingApp(PlatformAccentImpl.INSTANCE.getAccent(null));
        PlatformAccentImpl.INSTANCE.subscribeToChanges(testingApp::updateColor);
    }

    public void updateColor(Color newColor) {
        this.colorPanel.setBackground(newColor);
        this.colorPanel.repaint();
    }
}
