package org.redlance.platformtools.impl.windows;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.redlance.platformtools.PlatformFileReferer;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystemNotFoundException;
import java.util.*;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Confirmed to work on:
 * - Firefox
 * - Google Chrome
 *
 * Workarounds implemented:
 * - Some archivers pass the path to the archive from which the file was extracted.
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
            if (value != null && !value.isBlank()) {
                referrers.add(value);

                try {
                    String localPath = resolveLocalPath(value.trim());
                    if (localPath != null) {
                        referrers.addAll(getFileReferer(localPath));
                    }
                } catch (Throwable th) {
                    th.printStackTrace();
                }
            }
        }
        return Collections.unmodifiableSet(referrers);
    }

    @Nullable
    private static String resolveLocalPath(String v) {
        if (v.regionMatches(true, 0, "file:", 0, 5)) {
            try {
                URI uri = URI.create(v);
                Path path = Paths.get(uri);
                return path.toString();
            } catch (IllegalArgumentException | FileSystemNotFoundException | SecurityException e) {
                return null;
            }
        }

        if (v.startsWith("\\\\")) return v;

        if (v.length() >= 2 && Character.isLetter(v.charAt(0)) && v.charAt(1) == ':') {
            return v.replace('/', '\\');
        }

        return null;
    }
}
