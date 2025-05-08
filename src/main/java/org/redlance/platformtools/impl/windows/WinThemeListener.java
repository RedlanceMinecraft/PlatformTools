package org.redlance.platformtools.impl.windows;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.win32.StdCallLibrary;
import org.redlance.platformtools.impl.windows.jna.ExtendedUser32;
import org.redlance.platformtools.impl.windows.jna.WPARAM;

import java.awt.*;
import java.util.function.Consumer;

public class WinThemeListener implements StdCallLibrary.StdCallCallback {
    private final Pointer hwnd;
    private final Consumer<Color> consumer;

    private final Pointer originalWndProc;

    public WinThemeListener(Pointer hwnd, Consumer<Color> consumer) {
        this.hwnd = hwnd;
        this.consumer = consumer;

        this.originalWndProc = ExtendedUser32.INSTANCE.GetWindowLongPtr(hwnd, -4 /* GWLP_WNDPROC  */);
        Pointer result = ExtendedUser32.INSTANCE.SetWindowLongPtr(hwnd, -4 /* GWLP_WNDPROC  */, this);
        if (result == null) throw new IllegalStateException("Failed to set window proc hook. Error: " + Native.getLastError());
    }

    @SuppressWarnings("unused")
    public Pointer callback(Pointer hWnd, int uMsg, WPARAM wParam, WPARAM lParam) {
        if (this.hwnd.equals(hWnd)) {
            switch (uMsg) {
                /*case 0x001A, 0x031A -> { // WM_SETTINGCHANGE, WM_THEMECHANGED
                    System.out.println("Theme change message received in hook " + uMsg);
                }*/

                case 0x0320 -> { // WM_DWMCOLORIZATIONCOLORCHANGED
                    this.consumer.accept(new Color(wParam.intValue(), true));
                }
            }
        }

        if (this.originalWndProc != null) {
            return ExtendedUser32.INSTANCE.CallWindowProc(originalWndProc, hWnd, uMsg, wParam, lParam);
        }
        return ExtendedUser32.INSTANCE.DefWindowProc(hWnd, uMsg, wParam, lParam);
    }
}
