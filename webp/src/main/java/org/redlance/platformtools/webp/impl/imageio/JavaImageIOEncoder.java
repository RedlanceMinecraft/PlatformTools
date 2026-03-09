package org.redlance.platformtools.webp.impl.imageio;

import org.redlance.platformtools.webp.encoder.PlatformWebPEncoder;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
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
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByMIMEType("image/webp");
        return writers.hasNext() ? new JavaImageIOEncoder() : null;
    }

    @Override
    public String backendName() {
        return "Java ImageIO";
    }

    @Override
    public byte[] encodeLossless(byte[] rgba, int width, int height) {
        return encode(rgba, width, height, null);
    }

    @Override
    public byte[] encodeLossy(byte[] rgba, int width, int height, float quality) {
        return encode(rgba, width, height, quality);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    private byte[] encode(byte[] rgba, int width, int height, Float quality) {
        BufferedImage img = fromRGBA(rgba, width, height);

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

    private static BufferedImage fromRGBA(byte[] rgba, int width, int height) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
        byte[] abgr = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
        for (int i = 0; i < width * height; i++) {
            int si = i * 4;
            abgr[si]     = rgba[si + 3]; // A
            abgr[si + 1] = rgba[si + 2]; // B
            abgr[si + 2] = rgba[si + 1]; // G
            abgr[si + 3] = rgba[si];     // R
        }
        return img;
    }
}