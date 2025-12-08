package org.redlance.platformtools.impl.windows;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.redlance.platformtools.PlatformFileReferer;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.util.*;

/**
 * Confirmed to work on:
 * - Firefox
 * - Google Chrome
 *
 * Workarounds implemented:
 * - Default windows archiver pass the path to the archive from which the file was extracted.
 */
public class WindowsFileReferer implements PlatformFileReferer {
    private static final Set<String> KEYS = Set.of("HostUrl", "ReferrerUrl", "LastWriterPackageFamilyName");

    @Override
    public @NotNull Set<String> getFileReferer(String path) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(path + ":Zone.Identifier", StandardCharsets.UTF_8))) {
            Set<String> referrers = new HashSet<>(KEYS.size());

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                if (line.startsWith(";") || line.startsWith("#")) continue;
                if (line.startsWith("[") && line.endsWith("]")) continue;

                int eq = line.indexOf('=');
                if (eq <= 0) continue;

                String key = line.substring(0, eq).trim();
                String value = line.substring(eq + 1).trim();

                if (KEYS.contains(key) && !value.isBlank()) {
                    referrers.add(value);

                    try {
                        String localPath = resolveLocalPath(value.trim());
                        if (localPath != null) referrers.addAll(getFileReferer(localPath));
                    } catch (Throwable th) {
                        th.printStackTrace();
                    }
                }
            }

            return Collections.unmodifiableSet(referrers);
        } catch (FileNotFoundException | AccessDeniedException e) {
            return Collections.emptySet();
        }
    }

    @Nullable
    private static String resolveLocalPath(String referrerUrl) {
        if (referrerUrl.startsWith("file://")) {
            String path = referrerUrl.substring(7);
            if (path.startsWith("/") && path.length() > 2 && path.charAt(2) == ':') {
                path = path.substring(1);
            }
            return URLDecoder.decode(path, StandardCharsets.UTF_8);
        } else if (referrerUrl.startsWith("\\\\") || referrerUrl.matches("^[A-Z]:[/\\\\].*")) {
            return referrerUrl;

        }
        return null;
    }
}
