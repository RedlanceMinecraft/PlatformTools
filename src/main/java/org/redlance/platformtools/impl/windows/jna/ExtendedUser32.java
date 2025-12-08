package org.redlance.platformtools.impl.windows.jna;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

public interface ExtendedUser32 extends StdCallLibrary {
    ExtendedUser32 INSTANCE = Native.load("user32", ExtendedUser32.class, W32APIOptions.DEFAULT_OPTIONS);

    Pointer SetWindowLongPtr(Pointer hWnd, int nIndex, StdCallCallback dwNewLong);
    Pointer SetWindowLongPtr(Pointer hWnd, int nIndex, Pointer newLong);

    Pointer CallWindowProc(Pointer lpPrevWndFunc, Pointer hWnd, int Msg, WPARAM wParam, WPARAM lParam);

    boolean EnumWindows(WNDENUMPROC lpEnumFunc, Pointer data);
    int GetWindowThreadProcessId(Pointer hWnd, IntByReference lpdwProcessId);
    boolean IsWindowVisible(Pointer hWnd);
}
