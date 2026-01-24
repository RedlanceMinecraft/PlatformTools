# PlatformTools

**PlatformTools** is a lightweight Java library designed to provide deep integration with operating system features (macOS and Windows) that are not available in the standard JDK. It utilizes JNA (Java Native Access) to bridge the gap between Java and native OS APIs.

## ✨ Features

* **[Finder Favorites (macOS)](#1-finder-favorites-macos):** Programmatically pin, unpin, and check the status of folders in the macOS Finder Sidebar.
    * *Note: Support for Windows Quick Access is planned for future releases.*
* **[System Accent Color (Windows & macOS)](#2-system-accent-color):** Retrieve the user's system accent color and subscribe to real-time changes (via Windows DWM hooks or macOS `NSDistributedNotificationCenter`).
* **[File Origin Metadata (Windows & macOS)](#3-file-origin--referrer):** Retrieve the source URL of downloaded files.
    * **macOS:** Reads `kMDItemWhereFroms` metadata.
    * **Windows:** Reads **Alternate Data Streams (ADS)**, specifically `Zone.Identifier`.
* **Java 8 Compatible:** Built on Java 17 but distributed with a Java 8 compatible version (Multi-Release JAR).

## 📦 Installation
The library is hosted on the Redlance repository.

### Gradle (Groovy)
```groovy
repositories {
    mavenCentral()
    maven { url = uri("https://repo.redlance.org/public") }
}

dependencies {
    // For Java 17+ (Standard version)
    implementation "org.redlance:platformtools:3.1.3"

    // For Java 8 (Downgraded version)
    implementation("org.redlance:platformtools:3.1.3:java8")
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

<dependency>
    <groupId>org.redlance</groupId>
    <artifactId>platformtools</artifactId>
    <version>3.1.3</version>
</dependency>
```

## 🚀 Usage

### 1. Finder Favorites (macOS)
Manage the "Favorites" section in the macOS Finder Sidebar.

> **Note:** Windows "Quick Access" support is currently in development. On Windows, these methods will currently return `false` or perform no action.

```java
import org.redlance.platformtools.PlatformFinderFavorites;

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

### 2. System Accent Color
Get the system's personalization color and listen for changes in real-time.

```java
import org.redlance.platformtools.PlatformAccent;
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

### 3. File Origin / Referrer
Retrieve the source URLs for downloaded files. This works on both macOS (Metadata) and Windows (ADS).

```java
import org.redlance.platformtools.PlatformFileReferer;
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

## 🛠 System Requirements
* **Java:** 8 or higher.
* **Operating Systems:**
* **macOS:** 10.13 (High Sierra) or newer (x86_64 / arm64).
* **Windows:** Windows 10 or Windows 11 (x64).


* **Native Dependencies:**
* This library bundles the necessary JNA bindings.
* On macOS, it requires `java-objc-bridge` (included in dependencies).

## 📄 License
This project is licensed under the **MIT License**.
