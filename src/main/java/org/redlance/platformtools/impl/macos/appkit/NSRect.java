package org.redlance.platformtools.impl.macos.appkit;

import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

public class NSRect extends Structure implements Structure.ByValue {
    public static class NSPoint extends Structure {
        public double x;
        public double y;

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("x", "y");
        }
    }

    public static class NSSize extends Structure {
        public double width;
        public double height;

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("width", "height");
        }
    }

    public NSPoint origin = new NSPoint();
    public NSSize size = new NSSize();

    public NSRect(double x, double y, double width, double height) {
        this.origin.x = x;
        this.origin.y = y;
        this.size.width = width;
        this.size.height = height;
    }

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("origin", "size");
    }
}
