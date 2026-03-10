package org.redlance.platformtools.webp.impl.image;

import org.redlance.platformtools.webp.decoder.DecodedImage;

public final class DecodedImageImpl extends DecodedImage {
    private final int[] argb;
    private final int width;
    private final int height;

    public DecodedImageImpl(int[] argb, int width, int height) {
        this.argb = argb;
        this.width = width;
        this.height = height;
    }

    @Override
    public int[] argb() {
        return this.argb;
    }

    @Override
    public int width() {
        return this.width;
    }

    @Override
    public int height() {
        return this.height;
    }

    @Override
    public boolean isDecoded() {
        return true;
    }
}
