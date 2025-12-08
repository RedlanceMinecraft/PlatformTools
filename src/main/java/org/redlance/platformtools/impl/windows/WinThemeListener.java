package org.redlance.platformtools.impl.windows;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.win32.StdCallLibrary;
import org.redlance.platformtools.impl.windows.jna.ExtendedUser32;
import org.redlance.platformtools.impl.windows.jna.WPARAM;

import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

public class WinThemeListener implements StdCallLibrary.StdCallCallback {
    private static final int GWLP_WNDPROC = -4;
    private static final int WM_DWMCOLORIZATIONCOLORCHANGED = 0x0320;
    private static final int WM_NCDESTROY = 0x0082;

    private final Pointer hwnd;
    private final List<Consumer<Color>> consumers;

    private Pointer originalWndProc;

    public WinThemeListener(Pointer hwnd, List<Consumer<Color>> consumers) {
        this.hwnd = hwnd;
        this.consumers = consumers;

        Pointer prev = ExtendedUser32.INSTANCE.SetWindowLongPtr(hwnd, GWLP_WNDPROC, this);
        if (prev == null) {
            int err = Native.getLastError();
            throw new IllegalStateException("SetWindowLongPtr failed, error=" + err);
        }
        this.originalWndProc = prev;
    }

    @SuppressWarnings("unused")
    public Pointer callback(Pointer hWnd, int uMsg, WPARAM wParam, WPARAM lParam) {
        if (this.hwnd.equals(hWnd) && uMsg == WM_DWMCOLORIZATIONCOLORCHANGED) {
            Color color = new Color(wParam.intValue(), true);

            for (Consumer<Color> consumer : this.consumers) {
                consumer.accept(color);
            }
        }

        if (hWnd.equals(hwnd) && uMsg == WM_NCDESTROY) {
            ExtendedUser32.INSTANCE.SetWindowLongPtr(hwnd, GWLP_WNDPROC, this.originalWndProc);
            this.originalWndProc = null;
        }

        return ExtendedUser32.INSTANCE.CallWindowProc(this.originalWndProc, hWnd, uMsg, wParam, lParam);
    }

    public boolean isDestroyed() {
        return this.originalWndProc == null;
    }
}
