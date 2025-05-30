package org.redlance.platformtools.impl.macos;

import de.jangassen.jfa.ObjcToJava;
import de.jangassen.jfa.appkit.NSObject;
import de.jangassen.jfa.foundation.Foundation;
import de.jangassen.jfa.foundation.ID;
import org.jetbrains.annotations.Nullable;
import org.redlance.platformtools.PlatformFileReferer;
import org.redlance.platformtools.impl.macos.appkit.CoreServices;

import java.util.ArrayList;
import java.util.List;

public class MacFileReferer implements PlatformFileReferer {
    protected static final String ATTRIBUTE_NAME = "kMDItemWhereFroms";

    @Override
    public @Nullable String getFileReferer(String path) {
        ID mdItem = CoreServices.INSTANCE.MDItemCreate(null, Foundation.nsString(path));
        ID attribute = CoreServices.INSTANCE.MDItemCopyAttribute(mdItem, Foundation.nsString(ATTRIBUTE_NAME));
        if (attribute == null || attribute.equals(ID.NIL)) return null;

        List<NSObject> referrers = new ArrayList<>();
        Foundation.foreachCFArray(attribute, id -> {
            try {
                referrers.add(ObjcToJava.map(id, NSObject.class));
            } catch (Throwable ignored) {}
        });
        if (referrers.isEmpty()) return null;

        return referrers.get(0).toString();
    }
}
