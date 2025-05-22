package org.redlance.platformtools.impl;

import javax.swing.*;
import java.awt.*;

public class TestingApp extends JFrame {
    private final JPanel colorPanel;

    public TestingApp(Color initialColor) {
        setTitle("Color Window");
        setSize(400, 300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

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
