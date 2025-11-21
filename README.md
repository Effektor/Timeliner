# Timeliner

An Android application built with Kotlin.

## Build Instructions

### Prerequisites
- JDK 17 or higher
- Android SDK (automatically downloaded by Gradle)

### Building the App

#### Using Gradle Wrapper (Recommended)

**Debug Build:**
```bash
./gradlew assembleDebug
```

**Release Build:**
```bash
./gradlew assembleRelease
```

**Run Tests:**
```bash
./gradlew test
```

**Build Everything:**
```bash
./gradlew build
```

### Output Location

After building, you can find the APK files at:
- Debug APK: `app/build/outputs/apk/debug/app-debug.apk`
- Release APK: `app/build/outputs/apk/release/app-release-unsigned.apk`

## CI/CD

This project uses GitHub Actions for continuous integration. On every push or pull request to the `main` or `develop` branches, the workflow:

1. Builds the app (both debug and release variants)
2. Runs unit tests
3. Uploads APK artifacts that can be downloaded from the Actions tab

### Downloading Build Artifacts

To download build artifacts:
1. Go to the "Actions" tab in the GitHub repository
2. Click on the latest successful workflow run
3. Scroll down to the "Artifacts" section
4. Download the desired APK (app-debug or app-release)

## Project Structure

```
Timeliner/
├── app/                        # Main application module
│   ├── src/
│   │   └── main/
│   │       ├── java/           # Kotlin source files
│   │       ├── res/            # Resources (layouts, strings, etc.)
│   │       └── AndroidManifest.xml
│   └── build.gradle.kts        # App-level build configuration
├── gradle/                     # Gradle wrapper files
├── .github/
│   └── workflows/
│       └── android-build.yml   # CI/CD workflow
├── build.gradle.kts           # Project-level build configuration
├── settings.gradle.kts        # Gradle settings
└── gradlew                    # Gradle wrapper script (Unix)
```

## Development

This is a basic Android application template with:
- Kotlin as the primary language
- AndroidX libraries
- Material Design components
- Gradle build system

Feel free to extend and customize the app according to your needs!