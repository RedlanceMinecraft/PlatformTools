package org.redlance.platformtools.impl.windows;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;
import org.redlance.platformtools.PlatformAccent;
import org.redlance.platformtools.impl.windows.jna.DwmApi;
import org.redlance.platformtools.impl.windows.jna.ExtendedUser32;
import org.redlance.platformtools.impl.windows.jna.WPARAM;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class WindowsAccent implements PlatformAccent, StdCallLibrary.StdCallCallback {
    private static final int GWLP_WNDPROC = -4;
    private static final int WM_DWMCOLORIZATIONCOLORCHANGED = 0x0320;
    private static final int WM_NCDESTROY = 0x0082;

    private final List<Consumer<Color>> consumers = new ArrayList<>();

    private Pointer hwnd;
    private Pointer originalWndProc;

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

    @SuppressWarnings("unused")
    public Pointer callback(Pointer hWnd, int uMsg, WPARAM wParam, WPARAM lParam) {
        if (this.hwnd != null && this.hwnd.equals(hWnd)) {
            if (uMsg == WM_DWMCOLORIZATIONCOLORCHANGED) {
                Color color = new Color(wParam.intValue(), true);

                for (Consumer<Color> consumer : this.consumers) {
                    consumer.accept(color);
                }
            }

            if (uMsg == WM_NCDESTROY) {
                ExtendedUser32.INSTANCE.SetWindowLongPtr(hwnd, GWLP_WNDPROC, this.originalWndProc);
                this.originalWndProc = this.hwnd = null;
                hookToAnyWindow();
            }
        }

        return ExtendedUser32.INSTANCE.CallWindowProc(this.originalWndProc, hWnd, uMsg, wParam, lParam);
    }

    @Override
    public void subscribeToChanges(Consumer<Color> consumer) {
        if (this.originalWndProc == null || this.hwnd == null) hookToAnyWindow();
        this.consumers.add(consumer);
    }

    @Override
    public boolean unsubscribeFromChanges(Consumer<Color> consumer) {
        return this.consumers.remove(consumer);
    }

    private void hookToAnyWindow() {
        ExtendedUser32.INSTANCE.EnumWindows((hWnd, data) -> {
            IntByReference pidRef = new IntByReference();
            ExtendedUser32.INSTANCE.GetWindowThreadProcessId(hWnd, pidRef);

            if (pidRef.getValue() == ProcessHandle.current().pid() && ExtendedUser32.INSTANCE.IsWindowVisible(hWnd)) {
                this.hwnd = hWnd;

                Pointer prev = ExtendedUser32.INSTANCE.SetWindowLongPtr(this.hwnd, GWLP_WNDPROC, this);
                if (prev == null) throw new IllegalStateException("SetWindowLongPtr failed, error=" + Native.getLastError());
                this.originalWndProc = prev;
                return false;
            }
            return true;
        }, Pointer.NULL);
    }
}
