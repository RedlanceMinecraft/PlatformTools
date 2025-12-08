package org.redlance.platformtools.impl.windows;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.redlance.platformtools.PlatformFileReferer;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
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
 * - Some archivers pass the path to the archive from which the file was extracted.
 */
public class WindowsFileReferer implements PlatformFileReferer {
    private static final Set<String> KEYS = Set.of("HostUrl", "ReferrerUrl", "LastWriterPackageFamilyName");

    @Override
    public @NotNull Set<String> getFileReferer(String path) throws IOException {
        Properties props = new Properties();
        try (Reader reader = new FileReader(path + ":Zone.Identifier", StandardCharsets.UTF_8)) {
            props.load(reader);
        } catch (FileNotFoundException | AccessDeniedException ex) {
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
