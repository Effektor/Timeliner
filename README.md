# Timeliner

**AndroidTimelineCompanion** - An Android app that helps you export and upload your Google Maps Timeline data to a web service.

## What Does This App Do?

This app provides a streamlined workflow to:

1. **Select a folder** where you save Timeline export files (using Android's Storage Access Framework)
2. **Open Location Settings** with one tap to manually export your Google Maps Timeline data as JSON
3. **Automatically detect** the latest exported JSON file in your chosen folder
4. **Upload the Timeline data** to a configurable web service via HTTP POST

## Features

- ✅ Easy folder selection with persistent permissions
- ✅ One-tap access to Location/Timeline settings
- ✅ Automatic detection of the latest JSON export file
- ✅ HTTP upload with OkHttp and Kotlin Coroutines
- ✅ Clear status display showing folder, latest file, and last upload time
- ✅ Configurable endpoint URL via BuildConfig
- ✅ Material Design UI with progress indicators
- ✅ Graceful error handling for all operations

## Requirements

- **Android 7.0 (API 24)** or higher
- Internet permission for uploading data
- Storage permissions managed via Storage Access Framework (no legacy permissions needed)

## Build Instructions

### Prerequisites
- JDK 17 or higher
- Android SDK (automatically downloaded by Gradle)
- Internet access for downloading dependencies

### Using Make (Recommended)

The project includes a Makefile with convenient shortcuts:

```bash
# Show all available commands
make help

# Clean build artifacts
make clean

# Build debug APK
make assembleDebug

# Build release APK (unsigned)
make assembleRelease

# Run unit tests
make test

# Run lint checks
make lint

# Build everything and run tests
make build
```

### Using Gradle Wrapper Directly

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

## Configuration

### Changing the Upload Endpoint

The web service endpoint URL is currently configured in `app/build.gradle.kts`:

```kotlin
buildConfigField("String", "TIMELINE_UPLOAD_ENDPOINT", "\"https://example.com/api/timeline\"")
```

To change the endpoint:

1. Open `app/build.gradle.kts`
2. Find the `buildConfigField` line in `defaultConfig`
3. Change the URL to your desired endpoint
4. Rebuild the app

The endpoint is currently set to `https://example.com/api/timeline` as a placeholder.

### Release Signing

The current build configuration produces **unsigned release APKs**. To sign release builds:

1. Create a keystore file
2. Add signing configuration to `app/build.gradle.kts`:

```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file("path/to/your/keystore.jks")
            storePassword = "your-store-password"
            keyAlias = "your-key-alias"
            keyPassword = "your-key-password"
        }
    }
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            // ... other settings
        }
    }
}
```

**Important:** Never commit keystore files or passwords to version control.

## CI/CD

This project uses GitHub Actions for continuous integration. On every push or pull request to the `main` or `develop` branches, the workflow:

1. Builds the app (both debug and release variants)
2. Runs unit tests
3. Uploads APK artifacts that can be downloaded from the Actions tab

### Downloading Build Artifacts

To download build artifacts from CI:
1. Go to the "Actions" tab in the GitHub repository
2. Click on the latest successful workflow run
3. Scroll down to the "Artifacts" section
4. Download the desired APK (app-debug or app-release)

## Project Structure

```
Timeliner/
├── app/                                    # Main application module
│   ├── src/
│   │   └── main/
│   │       ├── java/com/effektor/timeliner/
│   │       │   ├── MainActivity.kt         # Main activity with UI logic
│   │       │   ├── network/
│   │       │   │   └── Uploader.kt         # HTTP upload functionality
│   │       │   └── repository/
│   │       │       ├── FolderRepository.kt # Folder URI persistence
│   │       │       └── TimelineFileRepository.kt # JSON file scanning
│   │       ├── res/
│   │       │   ├── layout/
│   │       │   │   └── activity_main.xml   # Main UI layout
│   │       │   └── values/
│   │       │       └── strings.xml         # String resources
│   │       └── AndroidManifest.xml
│   └── build.gradle.kts                    # App-level build configuration
├── gradle/                                 # Gradle wrapper files
├── .github/
│   └── workflows/
│       └── android-build.yml               # CI/CD workflow
├── build.gradle.kts                        # Project-level build configuration
├── settings.gradle.kts                     # Gradle settings
├── Makefile                                # Make shortcuts for Gradle tasks
├── gradlew                                 # Gradle wrapper script (Unix)
└── README.md                               # This file
```

## How to Use the App

1. **First Run - Choose Folder:**
   - Open the app
   - Tap "Choose Export Folder"
   - Select the folder where you want to save Timeline exports
   - The app will remember this folder

2. **Export Timeline Data:**
   - Tap "Open Location Settings"
   - Navigate to Location Services → Timeline
   - Export your Timeline data
   - Save the exported JSON file to the folder you selected in step 1

3. **Upload Timeline Data:**
   - Return to the app (it will automatically detect the new file)
   - Review the detected file information
   - Tap "Send Latest Export Now"
   - Wait for the upload to complete

The app will display:
- Currently selected folder
- Name and modification time of the latest JSON file
- Last successful upload timestamp
- Current endpoint URL

## Technical Details

### Technologies Used

- **Language:** Kotlin
- **Min SDK:** 24 (Android 7.0)
- **Target SDK:** 34 (Android 14)
- **Architecture Components:**
  - ActivityResult API for folder selection
  - Kotlin Coroutines for async operations
  - Storage Access Framework for folder access
- **Libraries:**
  - AndroidX Core, AppCompat, Material Design
  - DocumentFile for SAF operations
  - OkHttp for HTTP requests
  - WorkManager (included for future background upload feature)

### Security & Privacy

- The app requires INTERNET permission for uploading data
- No legacy storage permissions are used (uses Storage Access Framework)
- The endpoint URL is configurable and not hardcoded in source files
- All network operations run on background threads
- Comprehensive error handling prevents crashes

## Development

### Code Organization

- **MainActivity:** Handles UI, user interactions, and orchestrates repository/uploader calls
- **FolderRepository:** Manages persistent storage of folder URI and upload timestamps
- **TimelineFileRepository:** Scans folders and identifies JSON files
- **Uploader:** Handles HTTP POST requests to the web service

### Adding New Features

Some ideas for future enhancements:

- Background periodic uploads using WorkManager
- Support for multiple endpoints
- File upload history
- Automatic detection when new files are added
- Network constraints (WiFi-only, etc.)
- Upload progress tracking
- File preview before upload

## License

This project is open source. See the repository for license details.

## Support

For issues, questions, or contributions, please use the GitHub issue tracker.