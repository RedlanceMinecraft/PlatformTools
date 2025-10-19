package org.redlance.platformtools.impl.macos;

import ca.weblite.objc.RuntimeUtils;
import com.sun.jna.Pointer;
import org.jetbrains.annotations.NotNull;
import org.redlance.platformtools.PlatformFileReferer;
import org.redlance.platformtools.impl.macos.appkit.CoreFoundation;
import org.redlance.platformtools.impl.macos.appkit.CoreServices;

import java.util.*;

/**
 * Confirmed to work on:
 * - Safari
 * - Google Chrome
 */
public class MacFileReferer implements PlatformFileReferer {
    protected static final String ATTRIBUTE_NAME = "kMDItemWhereFroms";

    @Override
    public @NotNull Set<String> getFileReferer(String path) {
        Pointer mdItem = CoreServices.INSTANCE.MDItemCreate(null, RuntimeUtils.str(path));
        Pointer attribute = CoreServices.INSTANCE.MDItemCopyAttribute(mdItem, RuntimeUtils.str(ATTRIBUTE_NAME));
        if (attribute == null || attribute.equals(Pointer.NULL)) return Collections.emptySet();

        long count = CoreFoundation.INSTANCE.CFArrayGetCount(attribute);
        if (count == 0) return Collections.emptySet();

        Set<String> referrers = new HashSet<>();
        for (long i = 0; i < count; i++) {
            Pointer itemPtr = CoreFoundation.INSTANCE.CFArrayGetValueAtIndex(attribute, i);
            if (itemPtr == null) continue;

            try {
                referrers.add(RuntimeUtils.msgString(itemPtr, "UTF8String"));
            } catch (Throwable ignored) {}
        }

        if (referrers.isEmpty()) return Collections.emptySet();
        return Collections.unmodifiableSet(referrers);
    }
}
