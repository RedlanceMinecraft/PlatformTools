package org.redlance.platformtools.impl.windows.jna;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.win32.W32APIOptions;

public interface ExtendedUser32 extends User32 {
    ExtendedUser32 INSTANCE = Native.load("user32", ExtendedUser32.class, W32APIOptions.DEFAULT_OPTIONS);

    Pointer SetWindowLongPtr(HWND hWnd, int nIndex, WindowProc dwNewLong);
}
