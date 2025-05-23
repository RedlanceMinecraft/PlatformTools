package org.redlance.platformtools.impl.windows;

import org.redlance.platformtools.PlatformFileReferer;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class WindowsFileReferer implements PlatformFileReferer {
    @Override
    public String getFileReferer(String path) throws IOException {
        Properties props = new Properties();
        try (Reader reader = new FileReader(path + ":Zone.Identifier", StandardCharsets.UTF_8)) {
            props.load(reader);
        }

        return (String) props.getOrDefault("HostUrl",
                props.getOrDefault("ReferrerUrl",
                        props.get("LastWriterPackageFamilyName")
                )
        );
    }
}
