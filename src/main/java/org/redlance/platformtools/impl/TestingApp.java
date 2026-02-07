package org.redlance.platformtools.impl;

import org.redlance.platformtools.PlatformAccent;
import org.redlance.platformtools.PlatformFileReferer;
import org.redlance.platformtools.PlatformFinderFavorites;
import org.redlance.platformtools.PlatformProgressBars;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.Collection;

public class TestingApp extends JFrame {
    private final JPanel colorPanel;
    private final JLabel referrerLabel;

    public TestingApp(Color initialColor) {
        setTitle("Color Window");
        setSize(640, 480);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        this.colorPanel = new JPanel();
        this.colorPanel.setBackground(initialColor);
        /*this.colorPanel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                super.mouseMoved(e);
                NSHapticFeedbackManager.defaultPerformer().performFeedbackPattern(0, 1);
            }
        });*/
        add(this.colorPanel);

        JPanel controlsPanel = new JPanel();
        add(controlsPanel, BorderLayout.SOUTH);

        JButton progressBarButton = new JButton("Progress Bar");
        progressBarButton.addActionListener(this::onProgressBar);
        controlsPanel.add(progressBarButton);

        JButton pinFolderButton = new JButton("Pin folder");
        pinFolderButton.addActionListener(this::onPinFolder);
        controlsPanel.add(pinFolderButton);

        JButton recreateButton = new JButton("Recreate");
        recreateButton.addActionListener(this::onRecreate);
        controlsPanel.add(recreateButton);

        JButton chooseFileButton = new JButton("Select file");
        chooseFileButton.addActionListener(this::onChooseFile);
        controlsPanel.add(chooseFileButton);

        this.referrerLabel = new JLabel("Referrer:");
        controlsPanel.add(this.referrerLabel);

        setVisible(true);
    }

    public static void main(String[] args) {
        TestingApp testingApp = new TestingApp(PlatformAccent.INSTANCE.getAccent());
        PlatformAccent.INSTANCE.subscribeToChanges(testingApp::updateColor);
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
                Collection<String> referrer = PlatformFileReferer.INSTANCE.getFileReferer(fileChooser.getSelectedFile());
                this.referrerLabel.setText("Referrer: " + String.join(", ", referrer));
                System.out.println(referrer);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private void onPinFolder(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            showPinDialog(selectedFile);
        }
    }

    private void showPinDialog(File file) {
        JDialog dialog = new JDialog(this, "Manage Favorites", true); // true = modal
        dialog.setSize(300, 150);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        // --- Status Panel ---
        JLabel statusLabel = new JLabel("Loading...", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Dialog", Font.BOLD, 14));
        dialog.add(statusLabel, BorderLayout.CENTER);

        // --- Buttons Panel ---
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton btnPin = new JButton("Pin");
        JButton btnUnpin = new JButton("Unpin");

        buttonPanel.add(btnPin);
        buttonPanel.add(btnUnpin);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        // --- UI Update Logic ---
        Runnable updateStatus = () -> {
            boolean isPinned = PlatformFinderFavorites.INSTANCE.isPinned(file.getAbsolutePath());

            if (isPinned) {
                statusLabel.setText("PINNED");
                statusLabel.setForeground(Color.GREEN);
            } else {
                statusLabel.setText("NOT PINNED");
                statusLabel.setForeground(Color.RED);
            }
        };

        // --- Button Listeners ---
        btnPin.addActionListener(event -> {
            boolean success = PlatformFinderFavorites.INSTANCE.pin(
                    file.getAbsolutePath(),
                    file.isDirectory(),
                    PlatformFinderFavorites.Position.LAST
            );

            // Set text color based on result
            btnPin.setForeground(success ? Color.GREEN : Color.RED);

            // Reset neighbor button color
            btnUnpin.setForeground(Color.BLACK);

            updateStatus.run();
        });

        btnUnpin.addActionListener(event -> {
            boolean success = PlatformFinderFavorites.INSTANCE.unpin(file.getAbsolutePath());

            // Set text color based on result
            btnUnpin.setForeground(success ? Color.GREEN : Color.RED);

            // Reset neighbor button color
            btnPin.setForeground(Color.BLACK);

            updateStatus.run();
        });

        // Initial status check
        updateStatus.run();
        dialog.setVisible(true);
    }

    private void onRecreate(ActionEvent e) {
        JFrame frame = new JFrame("temp");
        frame.setVisible(true);
        dispose();
        setVisible(true);
        frame.dispose();

        dispose();
        setVisible(true);
        PlatformAccent.INSTANCE.resubscribe();
    }

    private void onProgressBar(ActionEvent e) {
        showProgressDialog();
    }

    private void showProgressDialog() {
        JDialog dialog = new JDialog(this, "Progress Bars Manager", false);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel barsPanel = new JPanel();
        barsPanel.setLayout(new BoxLayout(barsPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(barsPanel);
        dialog.add(scrollPane, BorderLayout.CENTER);

        JButton addButton = new JButton("Add Progress Bar");
        addButton.addActionListener(event -> {
            PlatformProgressBars.PlatformProgressBar bar = PlatformProgressBars.INSTANCE.create();
            bar.setMaxValue(100);

            JPanel barPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            barPanel.setBorder(BorderFactory.createEtchedBorder());

            JLabel label = new JLabel("Bar " + (barsPanel.getComponentCount() + 1) + ":");

            JSlider slider = new JSlider(0, 100, 0);
            slider.setPreferredSize(new Dimension(200, 30));
            slider.addChangeListener(ev -> bar.setValue(slider.getValue()));

            JButton removeButton = new JButton("X");
            removeButton.addActionListener(ev -> {
                bar.close();
                barsPanel.remove(barPanel);
                barsPanel.revalidate();
                barsPanel.repaint();
            });

            barPanel.add(label);
            barPanel.add(slider);
            barPanel.add(removeButton);

            barsPanel.add(barPanel);
            barsPanel.revalidate();
            barsPanel.repaint();
        });

        dialog.add(addButton, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
}
