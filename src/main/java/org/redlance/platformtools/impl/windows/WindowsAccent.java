package org.redlance.platformtools.impl.windows;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;
import org.redlance.platformtools.PlatformAccent;
import org.redlance.platformtools.impl.windows.jna.DwmApi;
import org.redlance.platformtools.impl.windows.jna.ExtendedUser32;
import org.redlance.platformtools.impl.windows.jna.WPARAM;

import java.awt.Color;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class WindowsAccent implements PlatformAccent, StdCallLibrary.StdCallCallback {
    private static final int GWLP_WNDPROC = -4;
    private static final int WM_DWMCOLORIZATIONCOLORCHANGED = 0x0320;
    private static final int WM_NCDESTROY = 0x0082;

    private final List<Consumer<Color>> consumers = new CopyOnWriteArrayList<>();

    private Pointer hwnd;
    private Pointer originalWndProc;

    @Override
    public Color getAccent(Supplier<Color> fallback) {
        IntByReference colorization = new IntByReference();
        IntByReference opaque = new IntByReference();

        var result = DwmApi.INSTANCE.DwmGetColorizationColor(colorization, opaque);
        if (result.intValue() == 0) { // S_OK
            return new Color(colorization.getValue(), true);
        }
        return fallback.get();
    }

    @SuppressWarnings("unused")
    public Pointer callback(Pointer hWnd, int uMsg, WPARAM wParam, WPARAM lParam) {
        Pointer nextProc = this.originalWndProc;

        if (this.hwnd != null && this.hwnd.equals(hWnd)) {
            if (uMsg == WM_DWMCOLORIZATIONCOLORCHANGED) {
                Color color = new Color(wParam.intValue(), true);

                for (Consumer<Color> consumer : this.consumers) {
                    consumer.accept(color);
                }
            }

            if (uMsg == WM_NCDESTROY) {
                ExtendedUser32.INSTANCE.SetWindowLongPtr(hwnd, GWLP_WNDPROC, nextProc);
                this.originalWndProc = this.hwnd = null;
                resubscribe();
            }
        }

        if (nextProc == null) return Pointer.NULL;
        return ExtendedUser32.INSTANCE.CallWindowProc(nextProc, hWnd, uMsg, wParam, lParam);
    }

    @Override
    public void subscribeToChanges(Consumer<Color> consumer) {
        this.consumers.add(consumer);
        resubscribe();
    }

    @Override
    public boolean unsubscribeFromChanges(Consumer<Color> consumer) {
        return this.consumers.remove(consumer);
    }

    @Override
    public synchronized void resubscribe() {
        if (this.originalWndProc != null && this.hwnd != null) return;
        if (this.consumers.isEmpty()) return;

        int currentPid = (int) ProcessHandle.current().pid();
        ExtendedUser32.INSTANCE.EnumWindows((hWnd, data) -> {
            IntByReference pidRef = new IntByReference();
            ExtendedUser32.INSTANCE.GetWindowThreadProcessId(hWnd, pidRef);

            if (pidRef.getValue() == currentPid && ExtendedUser32.INSTANCE.IsWindowVisible(hWnd)) {
                Pointer prev = ExtendedUser32.INSTANCE.SetWindowLongPtr(hWnd, GWLP_WNDPROC, this);
                if (prev != null) {
                    this.hwnd = hWnd;
                    this.originalWndProc = prev;
                    return false;
                }
            }
            return true;
        }, Pointer.NULL);
    }
}
