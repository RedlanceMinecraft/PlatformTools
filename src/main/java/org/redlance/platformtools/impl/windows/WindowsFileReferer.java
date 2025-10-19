package org.redlance.platformtools.impl.windows;

import org.jetbrains.annotations.NotNull;
import org.redlance.platformtools.PlatformFileReferer;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Confirmed to work on:
 * - Firefox
 * - Google Chrome
 */
public class WindowsFileReferer implements PlatformFileReferer {
    private static final Set<String> KEYS = Set.of("HostUrl", "ReferrerUrl", "LastWriterPackageFamilyName");

    @Override
    public @NotNull Set<String> getFileReferer(String path) throws IOException {
        Properties props = new Properties();
        try (Reader reader = new FileReader(path + ":Zone.Identifier", StandardCharsets.UTF_8)) {
            props.load(reader);
        } catch (FileNotFoundException ex) {
            return Collections.emptySet();
        }

        Set<String> referrers = new HashSet<>(KEYS.size());
        for (String key : KEYS) {
            String value = props.getProperty(key);
            if (value != null && !value.isBlank()) referrers.add(value);
        }
        return Collections.unmodifiableSet(referrers);
    }
}
