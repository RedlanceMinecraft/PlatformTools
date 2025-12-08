package org.redlance.platformtools.impl.windows.jna;

import com.sun.jna.Pointer;
import com.sun.jna.win32.StdCallLibrary;

@SuppressWarnings("unused")
public interface WNDENUMPROC extends StdCallLibrary.StdCallCallback {
    boolean callback(Pointer hWnd, Pointer data);
}
