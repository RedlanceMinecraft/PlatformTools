/*
 * MIT License
 *
 * Copyright (c) 2019-2021 Jannis Weis
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.redlance.platformtools.impl.macos;

import java.awt.*;

public final class MacOSColors {
    // 0.000000 0.478431 1.000000
    public static final Color ACCENT_BLUE = color(0.000000f, 0.478431f, 1.000000f);
    // 0.584314 0.239216 0.588235
    public static final Color ACCENT_LILAC = color(0.584314f, 0.239216f, 0.588235f);
    // 0.968627 0.309804 0.619608
    public static final Color ACCENT_ROSE = color(0.968627f, 0.309804f, 0.619608f);
    // 0.878431 0.219608 0.243137
    public static final Color ACCENT_RED = color(0.878431f, 0.219608f, 0.243137f);
    // 0.968627 0.509804 0.105882
    public static final Color ACCENT_ORANGE = color(0.968627f, 0.509804f, 0.105882f);
    // 0.988235 0.721569 0.152941
    public static final Color ACCENT_YELLOW = color(0.988235f, 0.721569f, 0.152941f);
    // 0.384314 0.729412 0.274510
    public static final Color ACCENT_GREEN = color(0.384314f, 0.729412f, 0.274510f);
    // 0.596078 0.596078 0.596078
    public static final Color ACCENT_GRAPHITE = color(0.596078f, 0.596078f, 0.596078f);

    private static Color color(final float r, final float g, final float b) {
        /*
         * For consistency with the native code we mirror the implementation of the float to int conversion
         * of the Color class.
         */
        return new Color((int) (r * 255 + 0.5), (int) (g * 255 + 0.5), (int) (b * 255 + 0.5));
    }
}
