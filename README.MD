# PlatformTools

**PlatformTools** is a lightweight Java library designed to provide deep integration with operating system features (macOS and Windows) that are not available in the standard JDK. It utilizes JNA (Java Native Access) to bridge the gap between Java and native OS APIs.

## Modules

The library is split into independent modules so you can include only what you need:

| Module | Artifact | Description |
|--------|----------|-------------|
| **common** | `platformtools-common` | Shared interfaces and native bindings |
| **accent** | `platformtools-accent` | System accent color (Windows & macOS) |
| **referer** | `platformtools-referer` | File origin/referrer metadata (Windows & macOS) |
| **favorites** | `platformtools-favorites` | Finder Sidebar favorites (macOS) |
| **progress** | `platformtools-progress` | Taskbar/dock progress bars (macOS) |

## Features

* **[System Accent Color](#1-system-accent-color)** (`accent`): Retrieve the user's system accent color and subscribe to real-time changes (via Windows DWM hooks or macOS `NSDistributedNotificationCenter`).
* **[File Origin Metadata](#2-file-origin--referrer)** (`referer`): Retrieve the source URL of downloaded files.
    * **macOS:** Reads `kMDItemWhereFroms` metadata.
    * **Windows:** Reads **Alternate Data Streams (ADS)**, specifically `Zone.Identifier`.
* **[Finder Favorites](#3-finder-favorites-macos)** (`favorites`): Programmatically pin, unpin, and check the status of folders in the macOS Finder Sidebar.
    * *Note: Support for Windows Quick Access is planned for future releases.*
* **[Taskbar Progress Bars](#4-taskbar-progress-bars)** (`progress`): Display progress indicators on the taskbar/dock icon.
    * Supports multiple stacked progress bars (up to 8).
    * *Note: Windows taskbar support is planned for future releases.*
* **Java 8 Compatible:** Built on Java 17 but distributed with a Java 8 compatible version (Multi-Release JAR).

## Installation
The library is hosted on the Redlance repository.

### Gradle (Groovy)
```groovy
repositories {
    mavenCentral()
    maven { url = uri("https://repo.redlance.org/public") }
}

dependencies {
    // Include only the modules you need:
    implementation "org.redlance:platformtools-accent:3.2.0"
    implementation "org.redlance:platformtools-referer:3.2.0"
    implementation "org.redlance:platformtools-favorites:3.2.0"
    implementation "org.redlance:platformtools-progress:3.2.0"

    // For Java 8 (Downgraded version), add classifier:
    // implementation("org.redlance:platformtools-accent:3.2.0:java8")
}
```

### Maven
```xml
<repositories>
    <repository>
        <id>redlance-public</id>
        <url>https://repo.redlance.org/public</url>
    </repository>
</repositories>

<!-- Include only the modules you need -->
<dependency>
    <groupId>org.redlance</groupId>
    <artifactId>platformtools-accent</artifactId>
    <version>3.2.0</version>
</dependency>
```

## Usage

### 1. System Accent Color
Get the system's personalization color and listen for changes in real-time.

```java
import org.redlance.platformtools.accent.PlatformAccent;
import java.awt.Color;

public class AccentExample {
    public void init() {
        // 1. Get current color (with a fallback)
        Color accent = PlatformAccent.INSTANCE.getAccent();
        updateUI(accent);

        // 2. Subscribe to OS events (efficient lazy subscription)
        PlatformAccent.INSTANCE.subscribeToChanges(newColor -> {
            System.out.println("System accent color changed to: " + newColor);
            updateUI(newColor);
        });
    }

    // Call this if your window is recreated (specifically for Windows hooks)
    public void onWindowRecreated() {
        PlatformAccent.INSTANCE.resubscribe();
    }
}
```

### 2. File Origin / Referrer
Retrieve the source URLs for downloaded files. This works on both macOS (Metadata) and Windows (ADS).

```java
import org.redlance.platformtools.referer.PlatformFileReferer;
import java.io.File;
import java.util.Set;

public class ReferrerExample {
    public void printOrigin(File file) {
        try {
            Set<String> referrers = PlatformFileReferer.INSTANCE.getFileReferer(file);

            if (!referrers.isEmpty()) {
                System.out.println("File downloaded from:");
                referrers.forEach(System.out::println);
            } else {
                System.out.println("No origin metadata found (or file is local).");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

### 3. Finder Favorites (macOS)
Manage the "Favorites" section in the macOS Finder Sidebar.

> **Note:** Windows "Quick Access" support is currently in development. On Windows, these methods will currently return `false` or perform no action.

```java
import org.redlance.platformtools.favorites.PlatformFinderFavorites;

public class FavoritesExample {
    public void toggleFavorite(String path) {
        // Check if the folder is already pinned
        if (PlatformFinderFavorites.INSTANCE.isPinned(path)) {
            System.out.println("Folder is pinned. Unpinning...");
            PlatformFinderFavorites.INSTANCE.unpin(path);
        } else {
            System.out.println("Pinning folder to the sidebar...");
            PlatformFinderFavorites.INSTANCE.pin(
                path,
                true, // isDirectory
                PlatformFinderFavorites.Position.LAST // or Position.FIRST
            );
        }
    }
}
```

### 4. Taskbar Progress Bars
Display progress indicators on the taskbar/dock icon. Supports multiple stacked progress bars.

> **Note:** Windows taskbar support is currently in development. On Windows, these methods will currently perform no action.

```java
import org.redlance.platformtools.progress.PlatformProgressBars;
import org.redlance.platformtools.progress.PlatformProgressBars.PlatformProgressBar;

public class ProgressExample {
    public void downloadFile() {
        // Create a progress bar
        try (PlatformProgressBar progress = PlatformProgressBars.INSTANCE.create()) {
            progress.setMaxValue(100);

            for (int i = 0; i <= 100; i++) {
                progress.setValue(i);
                Thread.sleep(50);
            }
        } // Automatically closes and removes from dock
    }

    public void multipleProgressBars() {
        // Create multiple progress bars (up to 8)
        PlatformProgressBar bar1 = PlatformProgressBars.INSTANCE.create();
        PlatformProgressBar bar2 = PlatformProgressBars.INSTANCE.create();

        bar1.setMaxValue(100);
        bar2.setMaxValue(50);

        bar1.setValue(75);
        bar2.setValue(25);

        // Clean up when done
        bar1.close();
        bar2.close();
    }
}
```

## System Requirements
* **Java:** 8 or higher.
* **Operating Systems:**
* **macOS:** 10.9 (Mavericks) or newer (x86_64 / arm64).
* **Windows:** Windows 10 or Windows 11 (x64).


* **Native Dependencies:**
* This library bundles the necessary JNA bindings.
* On macOS, it requires `java-objc-bridge` (included in dependencies).

## License
This project is licensed under the **MIT License**.
