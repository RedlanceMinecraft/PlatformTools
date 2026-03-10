package org.redlance.platformtools.webp.impl.image;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.redlance.platformtools.webp.decoder.DecodedImage;
import org.redlance.platformtools.webp.decoder.PlatformWebPDecoder;

public final class LazyDecodedImage extends DecodedImage {
    private byte @Nullable [] webpData;
    private @Nullable DecodedImage delegate;

    public LazyDecodedImage(byte @NotNull [] webpData) {
        this.webpData = webpData;
    }

    private synchronized @NotNull DecodedImage ensureDecoded() {
        if (this.delegate == null) {
            this.delegate = PlatformWebPDecoder.INSTANCE.decode(this.webpData);
            this.webpData = null;
        }
        return this.delegate;
    }

    @Override
    public int[] argb() {
        return ensureDecoded().argb();
    }

    @Override
    public int width() {
        return ensureDecoded().width();
    }

    @Override
    public int height() {
        return ensureDecoded().height();
    }

    @Override
    public boolean isDecoded() {
        return this.delegate != null;
    }
}
