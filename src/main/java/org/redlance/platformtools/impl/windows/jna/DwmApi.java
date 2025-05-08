package org.redlance.platformtools.impl.windows.jna;

import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.ptr.ByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

public interface DwmApi extends StdCallLibrary {
    DwmApi INSTANCE = Native.load("dwmapi", DwmApi.class, W32APIOptions.DEFAULT_OPTIONS);

    /**
     * More at <a href="https://learn.microsoft.com/en-us/windows/win32/api/dwmapi/nf-dwmapi-dwmgetcolorizationcolor">learn.microsoft.com</a>
     * @param pcrColorization A pointer to a value that, when this function returns successfully, receives the current color used for glass composition. The color format of the value is 0xAARRGGBB.
     * @param pfOpaqueBlend A pointer to a value that, when this function returns successfully, indicates whether the color is an opaque blend. TRUE if the color is an opaque blend; otherwise, FALSE.
     * @return If this function succeeds, it returns S_OK. Otherwise, it returns an HRESULT error code.
     */
    NativeLong DwmGetColorizationColor(IntByReference pcrColorization, ByReference pfOpaqueBlend);
}
