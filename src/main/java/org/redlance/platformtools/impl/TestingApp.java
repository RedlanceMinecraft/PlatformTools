package org.redlance.platformtools.impl;

import de.jangassen.jfa.appkit.NSHapticFeedbackManager;
import com.sun.jna.Native;
import org.redlance.platformtools.PlatformAccent;
import org.redlance.platformtools.PlatformFileReferer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

public class TestingApp extends JFrame {
    private final JPanel colorPanel;
    private final JLabel referrerLabel;

    public TestingApp(Color initialColor) {
        setTitle("Color Window");
        setSize(400, 300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        this.colorPanel = new JPanel();
        this.colorPanel.setBackground(initialColor);
        this.colorPanel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                super.mouseMoved(e);
                NSHapticFeedbackManager.defaultPerformer().performFeedbackPattern(0, 1);
            }
        });
        add(this.colorPanel);

        JPanel controlsPanel = new JPanel();
        add(controlsPanel, BorderLayout.SOUTH);

        JButton chooseFileButton = new JButton("Select file");
        chooseFileButton.addActionListener(this::onChooseFile);
        controlsPanel.add(chooseFileButton);

        this.referrerLabel = new JLabel("Referrer:");
        controlsPanel.add(this.referrerLabel);

        setVisible(true);
    }

    public static void main(String[] args) {
        TestingApp testingApp = new TestingApp(PlatformAccent.INSTANCE.getAccent(() -> Color.BLUE));
        PlatformAccent.INSTANCE.subscribeToChanges(Native.getWindowPointer(testingApp), testingApp::updateColor);
    }

    public void updateColor(Color newColor) {
        this.colorPanel.setBackground(newColor);
        this.colorPanel.repaint();
    }

    private void onChooseFile(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                String referrer = PlatformFileReferer.INSTANCE.getFileReferer(fileChooser.getSelectedFile());
                this.referrerLabel.setText("Referrer: " + referrer);
                System.out.println(referrer);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
