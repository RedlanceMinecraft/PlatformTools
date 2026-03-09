package org.redlance.platformtools.testing;

import org.redlance.platformtools.accent.PlatformAccent;
import org.redlance.platformtools.favorites.PlatformFinderFavorites;
import org.redlance.platformtools.progress.PlatformProgressBars;
import org.redlance.platformtools.referer.PlatformFileReferer;
import org.redlance.platformtools.webp.decoder.DecodedImage;
import org.redlance.platformtools.webp.decoder.PlatformWebPDecoder;
import org.redlance.platformtools.webp.encoder.PlatformWebPEncoder;
import org.redlance.platformtools.webp.impl.ngengine.NgEngineDecoder;
import org.redlance.platformtools.webp.impl.libwebp.LibWebPDecoder;
import org.redlance.platformtools.webp.impl.libwebp.LibWebPEncoder;
import org.redlance.platformtools.webp.impl.macos.MacOSImageIODecoder;
import org.redlance.platformtools.webp.impl.windows.WindowsCodecsDecoder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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

        JButton webpButton = new JButton("WebP");
        webpButton.addActionListener(this::onWebP);
        controlsPanel.add(webpButton);

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

    private void onWebP(ActionEvent e) {
        showWebPDialog();
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

        int maxBars = PlatformProgressBars.INSTANCE.getMaxBars();

        JButton addButton = new JButton("Add Progress Bar" + (maxBars > 0 ? " (max " + maxBars + ")" : ""));
        addButton.addActionListener(event -> {
            PlatformProgressBars.PlatformProgressBar bar;
            try {
                bar = PlatformProgressBars.INSTANCE.create();
            } catch (PlatformProgressBars.TooManyProgressBarsException ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Limit reached", JOptionPane.WARNING_MESSAGE);
                return;
            }

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

    private void showWebPDialog() {
        JDialog dialog = new JDialog(this, "WebP Test", false);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JTextArea log = new JTextArea();
        log.setEditable(false);
        log.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        dialog.add(new JScrollPane(log), BorderLayout.CENTER);

        // Status
        boolean decodeAvailable = PlatformWebPDecoder.INSTANCE.isAvailable();
        boolean encodeAvailable = PlatformWebPEncoder.INSTANCE.isAvailable();
        log.append("Decoder: " + (decodeAvailable
                ? "available (" + PlatformWebPDecoder.INSTANCE.backendName() + ")"
                : "unavailable") + "\n");
        log.append("Encoder: " + (encodeAvailable
                ? "available (" + PlatformWebPEncoder.INSTANCE.backendName() + ")"
                : "unavailable") + "\n\n");

        JPanel buttons = new JPanel(new FlowLayout());

        // Probe each backend individually
        JButton probeBtn = new JButton("Probe Backends");
        probeBtn.addActionListener(event -> {
            log.append("--- Probing backends ---\n");

            try {
                PlatformWebPDecoder libDec = LibWebPDecoder.tryCreate();
                PlatformWebPEncoder libEnc = LibWebPEncoder.tryCreate();
                log.append("  libwebp: decode " + (libDec != null ? "OK" : "not found")
                        + ", encode " + (libEnc != null ? "OK" : "not found") + "\n");
            } catch (Throwable t) {
                log.append("  libwebp: ERROR " + t + "\n");
            }

            try {
                MacOSImageIODecoder macosDec = MacOSImageIODecoder.create();
                log.append("  macOS ImageIO: decode OK\n");
            } catch (Throwable t) {
                log.append("  macOS ImageIO decode: " + t + "\n");
            }
            try {
                // MacOSImageIOEncoder macosEnc = MacOSImageIOEncoder.tryCreate();
                log.append("  macOS ImageIO: encode " + (/*macosEnc != null ? "OK" :*/ "not available") + "\n");
            } catch (Throwable t) {
                log.append("  macOS ImageIO encode: ERROR " + t + "\n");
            }

            try {
                WindowsCodecsDecoder wicDec = WindowsCodecsDecoder.tryCreate();
                // WindowsCodecsEncoder wicEnc = WindowsCodecsEncoder.tryCreate();
                log.append("  Windows WIC: decode " + (wicDec != null ? "OK" : "not available")
                        + ", encode " + (/*wicEnc != null ? "OK" :*/ "not available") + "\n");
            } catch (Throwable t) {
                log.append("  Windows WIC: ERROR " + t + "\n");
            }

            try {
                NgEngineDecoder ngDec = NgEngineDecoder.tryCreate();
                log.append("  ngengine: decode " + (ngDec != null ? "OK" : "not found") + "\n");
            } catch (Throwable t) {
                log.append("  ngengine: ERROR " + t + "\n");
            }

            log.append("\n");
        });
        buttons.add(probeBtn);

        // Encode test
        JButton encodeBtn = new JButton("Encode Test");
        encodeBtn.setEnabled(encodeAvailable);
        encodeBtn.addActionListener(event -> {
            int w = 256, h = 256;
            int[] argb = new int[w * h];
            java.util.Random rng = new java.util.Random(42);
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    int r = Math.min(255, (x + rng.nextInt(32)) & 0xFF);
                    int g = Math.min(255, (y + rng.nextInt(32)) & 0xFF);
                    int b = rng.nextInt(256);
                    argb[y * w + x] = (0xFF << 24) | (r << 16) | (g << 8) | b;
                }
            }

            log.append("Encode 256x256 noisy gradient:\n");
            log.append("  Raw ARGB:  " + argb.length + " pixels\n");

            byte[] lossless = null;
            try {
                lossless = PlatformWebPEncoder.INSTANCE.encodeLossless(argb, w, h);
                log.append("  Lossless:  " + lossless.length + " bytes\n");
            } catch (Exception ex) {
                log.append("  Lossless:  FAILED: " + ex.getMessage() + "\n");
            }

            try {
                byte[] lossy75 = PlatformWebPEncoder.INSTANCE.encodeLossy(argb, w, h, 0.75f);
                log.append("  Lossy 75%%: " + lossy75.length + " bytes\n");
            } catch (Exception ex) {
                log.append("  Lossy 75%%: FAILED: " + ex.getMessage() + "\n");
            }

            try {
                byte[] lossy50 = PlatformWebPEncoder.INSTANCE.encodeLossy(argb, w, h, 0.50f);
                log.append("  Lossy 50%%: " + lossy50.length + " bytes\n");
            } catch (Exception ex) {
                log.append("  Lossy 50%%: FAILED: " + ex.getMessage() + "\n");
            }

            // Roundtrip test
            if (lossless != null && decodeAvailable) {
                try {
                    DecodedImage decoded = PlatformWebPDecoder.INSTANCE.decode(lossless);
                    log.append("  Roundtrip: " + decoded.width() + "x" + decoded.height()
                            + " (" + decoded.argb().length + " pixels)\n");
                } catch (Exception ex) {
                    log.append("  Roundtrip: FAILED: " + ex.getMessage() + "\n");
                }
            }
            log.append("\n");
        });
        buttons.add(encodeBtn);

        // Decode file
        JButton decodeBtn = new JButton("Decode File");
        decodeBtn.setEnabled(decodeAvailable);
        decodeBtn.addActionListener(event -> {
            JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("WebP files", "webp"));
            if (fc.showOpenDialog(dialog) != JFileChooser.APPROVE_OPTION) return;

            try {
                byte[] webpData = Files.readAllBytes(fc.getSelectedFile().toPath());
                log.append("File: " + fc.getSelectedFile().getName() + " (" + webpData.length + " bytes)\n");

                try {
                    int[] info = PlatformWebPDecoder.INSTANCE.getInfo(webpData);
                    log.append("  Info: " + info[0] + "x" + info[1] + "\n");
                } catch (Exception ex) {
                    log.append("  Info: FAILED: " + ex.getMessage() + "\n");
                }

                try {
                    DecodedImage decoded = PlatformWebPDecoder.INSTANCE.decode(webpData);
                    log.append("  Decoded: " + decoded.width() + "x" + decoded.height() + "\n");

                    // Show decoded image
                    BufferedImage img = new BufferedImage(decoded.width(), decoded.height(), BufferedImage.TYPE_INT_ARGB);
                    img.setRGB(0, 0, decoded.width(), decoded.height(), decoded.argb(), 0, decoded.width());

                    JDialog preview = new JDialog(dialog, "Preview: " + fc.getSelectedFile().getName(), false);
                    preview.add(new JLabel(new ImageIcon(img)));
                    preview.pack();
                    preview.setLocationRelativeTo(dialog);
                    preview.setVisible(true);
                } catch (Exception ex) {
                    log.append("  Decode: FAILED: " + ex.getMessage() + "\n");
                }
            } catch (IOException ex) {
                log.append("  Error: " + ex.getMessage() + "\n");
            }
            log.append("\n");
        });
        buttons.add(decodeBtn);

        dialog.add(buttons, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
}
