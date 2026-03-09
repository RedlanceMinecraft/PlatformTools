package org.redlance.platformtools.webp.impl.imageio;

import org.redlance.platformtools.webp.encoder.PlatformWebPEncoder;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;

/**
 * Fallback WebP encoder using {@link javax.imageio.ImageIO}.
 *
 * <p>Works when a WebP ImageIO plugin is available on the classpath
 * (e.g. TwelveMonkeys, or a JRE with built-in WebP support).
 */
public final class JavaImageIOEncoder implements PlatformWebPEncoder {
    private JavaImageIOEncoder() {
    }

    public static JavaImageIOEncoder tryCreate() {
        ImageIO.scanForPlugins();
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByMIMEType("image/webp");
        return writers.hasNext() ? new JavaImageIOEncoder() : null;
    }

    @Override
    public String backendName() {
        return "Java ImageIO";
    }

    @Override
    public byte[] encodeLossless(int[] argb, int width, int height) {
        return encode(argb, width, height, null);
    }

    @Override
    public byte[] encodeLossy(int[] argb, int width, int height, float quality) {
        return encode(argb, width, height, quality);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    private byte[] encode(int[] argb, int width, int height, Float quality) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        img.setRGB(0, 0, width, height, argb, 0, width);

        Iterator<ImageWriter> writers = ImageIO.getImageWritersByMIMEType("image/webp");
        if (!writers.hasNext()) {
            throw new IllegalStateException("No ImageIO writer for WebP");
        }

        ImageWriter writer = writers.next();
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            writer.setOutput(ImageIO.createImageOutputStream(baos));

            ImageWriteParam param = writer.getDefaultWriteParam();
            if (quality != null && param.canWriteCompressed()) {
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionType(param.getCompressionTypes()[0]);
                param.setCompressionQuality(quality);
            }

            writer.write(null, new IIOImage(img, null, null), param);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Java ImageIO encode failed", e);
        } finally {
            writer.dispose();
        }
    }

}