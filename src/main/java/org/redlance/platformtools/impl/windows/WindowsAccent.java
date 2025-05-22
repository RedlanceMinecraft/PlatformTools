package org.redlance.platformtools.impl.windows;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import org.jetbrains.annotations.Nullable;
import org.redlance.platformtools.PlatformAccent;
import org.redlance.platformtools.impl.utils.StubFrame;
import org.redlance.platformtools.impl.windows.jna.DwmApi;

import java.awt.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class WindowsAccent implements PlatformAccent {
    @Override
    public Color getAccent(Supplier<Color> fallback) {
        IntByReference colorization = new IntByReference();
        IntByReference opaque = new IntByReference();

        var result = DwmApi.INSTANCE.DwmGetColorizationColor(colorization, opaque);
        if (result.intValue() == 0) {
            return new Color(colorization.getValue());
        }
        return fallback.get();
    }

    @Override
    public void subscribeToChanges(@Nullable Long window, Consumer<Color> consumer) {
        if (window == null) window = StubFrame.WINDOW_ID;
        new WinThemeListener(new Pointer(window), consumer);
    }
}
