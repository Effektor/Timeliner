# agents.md

## Agent: AndroidTimelineCompanion

### High-level goal

Create a small Android app written in **Kotlin** that helps the user:

1. Quickly navigate to the Android **Location / Timeline** settings so they can manually export their Google Maps Timeline data as a JSON file.
2. Select and remember a **folder** where they will save Timeline exports.
3. Detect the **latest exported JSON file** in that folder and **upload it to a configurable web service** via HTTP POST.
4. Provide a simple UI that makes this workflow obvious and low-friction.
5. Build cleanly in CI using **GitHub Actions**, producing release artifacts (APK and/or AAB) as downloadable build assets.

The goal is a **single-module, minimal** Android app that targets modern Android and follows best practices (Kotlin, coroutines, WorkManager if background tasks are used).

---

## Functional requirements

1. **Onboarding / storage access**

   * On first run, the app should prompt the user to **choose a folder** where they will save Timeline export files (e.g. “Timeline exports”).
   * Use the **Storage Access Framework**:

     * Launch `Intent.ACTION_OPEN_DOCUMENT_TREE` to let the user pick a folder.
     * Persist the returned URI with `takePersistableUriPermission`.
   * Store the folder URI string in persistent storage (e.g. `SharedPreferences`).

2. **Open location settings**

   * The app must have a button: **“Open location settings to export Timeline”**.
   * When pressed:

     * Launch an intent with `Settings.ACTION_LOCATION_SOURCE_SETTINGS`.
     * The app cannot deep-link directly to the Timeline screen; just get the user into Location settings.
   * In the UI, clearly explain:

     * “Go to Location services → Timeline → Export Timeline data and save it into the chosen folder.”

3. **Find latest Timeline JSON**

   * The app should be able to scan the previously selected folder and find the **most recently modified JSON file**.
   * Use `DocumentFile.fromTreeUri(context, treeUri)` and `listFiles()`.
   * Filter for files with `.json` extension (case-insensitive).
   * Pick the file with maximum `lastModified()`.
   * Handle edge cases:

     * No folder selected.
     * Folder empty.
     * No `.json` files.
     * Missing permission or `null` URIs.

4. **Upload to web service**

   * Configuration:

     * For this version, hardcode a **placeholder endpoint** like `https://example.com/api/timeline`.
     * Make it easy to later move this to a build config or settings screen.
   * Implement a function that:

     * Opens an `InputStream` from the selected `DocumentFile` URI.
     * Reads the file as raw bytes and interprets as UTF-8 JSON.
     * Sends the JSON to the configured endpoint via HTTP POST with header:

       * `Content-Type: application/json`
     * Use **OkHttp** (or Ktor client) with coroutines.
   * Handle responses:

     * If successful (HTTP 2xx), show a success message and store “last successful upload time”.
     * If failed, show an error message and log details (e.g. via `Log.e`).

5. **User interface / UX**

   * Single-activity app (e.g. `MainActivity` with a simple layout).
   * UI elements:

     * Text explaining the workflow.
     * Button: “Choose export folder”

       * Shows currently selected folder (e.g. last segment of URI or a friendly label).
     * Button: “Open location settings to export Timeline”.
     * Button: “Send latest export now”.
     * A status text area showing:

       * Selected folder (or “Not selected”).
       * Name and modified time of the detected latest JSON file (or a message if none).
       * Last upload result and timestamp.
   * UX behavior:

     * If the folder is not configured, disable “Send latest export now” and show a friendly message.
     * After returning to the app (e.g. from Settings), it should refresh the folder content and show the latest file if any.
     * “Send latest export now”:

       * Runs the upload logic.
       * Shows a progress indicator (e.g. simple indeterminate progress bar or disabling the button while in progress).

6. **Optional background upload (if implemented)**

   * If you implement background uploads:

     * Use **WorkManager** to periodically check the folder for newer exports than the last successfully uploaded file.
     * Only upload when a newer file is found.
     * Respect basic constraints if desired (e.g. only on unmetered network).
   * This is optional; the minimum requirement is **manual “Send latest export now”**.

---

## Non-functional requirements

1. **Language / tech stack**

   * Kotlin only (no Java sources).
   * Use:

     * AndroidX libraries.
     * Coroutines (`kotlinx-coroutines-android`).
     * OkHttp (or Ktor) for HTTP.
     * View-based UI (simple `Activity` + XML layout) is enough; Compose is allowed but not required.

2. **Android version / SDK**

   * Target the latest stable SDK (e.g. 34 or whatever is current when you implement).
   * Minimum SDK: 24 (Android 7.0) or higher.
   * Use **scoped storage** practices, no legacy storage hacks.

3. **Project structure**

   * Single-module Gradle project: `app/` module.
   * Use **Android Gradle Plugin** and **Gradle Kotlin DSL** (`build.gradle.kts`).

4. **Config / secrets**

   * The web service URL should be defined in a single place:

     * Either as a constant in Kotlin, or
     * Preferably via `BuildConfig` field set in `build.gradle.kts`.
   * For this exercise, do **not** implement secure secret management; assume endpoint is non-secret.

5. **Logging / errors**

   * Use `Log.d` / `Log.e` and user-facing `Toast` or Snackbar messages for major actions and errors.
   * Make sure error cases do not crash the app.

---

## Implementation details for the agent

### 1. Project setup

* Create a standard Android app project with:

  * `settings.gradle.kts`
  * `build.gradle.kts` at root (reactor/application-level)
  * `app/build.gradle.kts`
* Configure:

  * `compileSdk` to latest.
  * `minSdk = 24`.
  * `targetSdk` = latest.
* Dependencies to include (approximate, versions should be current):

  * `implementation("androidx.core:core-ktx:...")`
  * `implementation("androidx.appcompat:appcompat:...")`
  * `implementation("com.google.android.material:material:...")`
  * `implementation("androidx.activity:activity-ktx:...")`
  * `implementation("androidx.constraintlayout:constraintlayout:...")`
  * Coroutines:

    * `implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:...")`
  * HTTP:

    * `implementation("com.squareup.okhttp3:okhttp:...")`
  * WorkManager (if background work is implemented):

    * `implementation("androidx.work:work-runtime-ktx:...")`

### 2. Permissions

* In `AndroidManifest.xml`, request:

  * `android.permission.INTERNET`
* **No** legacy storage permissions (`READ_EXTERNAL_STORAGE`, `WRITE_EXTERNAL_STORAGE`) are required if using SAF correctly.

### 3. Activity logic

`MainActivity` responsibilities:

1. On start:

   * Load stored tree URI string from `SharedPreferences` (if any).
   * If present, ensure that `DocumentFile.fromTreeUri()` returns non-null; if null, treat as invalid and ask user to re-select folder.
   * Update UI to display folder status and detected latest file.

2. Button: “Choose export folder”

   * Launch `ACTION_OPEN_DOCUMENT_TREE` with `startActivityForResult` or the Activity Result API.
   * On result:

     * Persist URI permission.
     * Store URI string in preferences.
     * Refresh displayed folder and latest file.

3. Button: “Open location settings to export Timeline”

   * Start `Settings.ACTION_LOCATION_SOURCE_SETTINGS`.

4. Button: “Send latest export now”

   * If no folder selected, show message and return.
   * Scan folder for latest JSON file.
   * If none found, show friendly error.
   * If found:

     * Launch a coroutine (e.g. `lifecycleScope.launch`) to:

       * Read file content.
       * POST to web service.
       * Update “last upload” status.

5. Handle Activity resume:

   * On `onResume`, re-scan folder to show if a new JSON file appeared while the user was in Settings.

### 4. Storage / config helpers

Implement helper classes / functions:

* `FolderRepository`

  * Responsible for storing and retrieving the selected tree URI string from `SharedPreferences`.
* `TimelineFileRepository`

  * Responsible for:

    * Given the tree URI, returning the `DocumentFile` of the folder.
    * Listing and returning the latest JSON `DocumentFile`.
* `Uploader`

  * Given a `DocumentFile`, reads bytes and posts to configured endpoint via OkHttp.

---

## Build & CI: Make/build script + GitHub Actions

### Goals

* Provide a simple way to **build the app locally** (CLI) and in **GitHub Actions**.
* Use a **Makefile** at the root to wrap Gradle commands.
* GitHub Actions workflow should:

  * Use the Android SDK.
  * Build the **release** variant.
  * Upload resulting APK (and/or AAB) as workflow artifacts.

### 1. Makefile requirements

Create a `Makefile` at the repository root with at least the following targets:

1. `make clean`

   * Runs Gradle clean.
2. `make assembleDebug`

   * Builds the debug APK.
3. `make assembleRelease`

   * Builds the release APK.
4. `make test`

   * Runs unit tests.
5. `make lint` (optional)

   * Runs Android lint or `./gradlew lint`.

Implementation expectations:

* Use the Gradle wrapper (`./gradlew`).
* Example commands (exact code to be written by the agent, not here):

  * `./gradlew clean`
  * `./gradlew :app:assembleDebug`
  * `./gradlew :app:assembleRelease`
  * `./gradlew test`
  * `./gradlew lint`

### 2. GitHub Actions workflow

Create a workflow file at `.github/workflows/android-build.yml` with the following characteristics:

1. **Triggers**

   * On `push` to `main` and `pull_request`.
   * Optionally on tags like `v*` if desired.

2. **Environment**

   * Use a `ubuntu-latest` runner.
   * Install Java (e.g. Temurin) via `actions/setup-java`.
   * Use the Gradle wrapper; optionally enable Gradle caching.

3. **Steps**

   * `actions/checkout` to get repo.
   * Set up JDK (e.g. Java 17).
   * Run `./gradlew assembleRelease`.
   * Collect build artifacts:

     * For example, `app/build/outputs/apk/release/*.apk`.
     * Optionally, `app/build/outputs/bundle/release/*.aab` if the Gradle project is configured to build bundles.
   * Use `actions/upload-artifact` to upload these files so they are downloadable from the workflow run.

4. **Signing**

   * For this baseline, **do not configure release signing with real keys**.
   * Let the build produce an **unsigned release APK** or use debug signing for artifacts.
   * Mark in comments where real signing config would go if needed.

Note: The agent should write the full YAML workflow file and Makefile, including comments that explain how to use them.

---

## Deliverables expected from the agent

1. **Source code**

   * A complete Android project with Kotlin sources, manifests, Gradle scripts, and a simple XML layout.
   * Clear package name (e.g. `com.example.timelinecompanion`).

2. **Documentation**

   * This `agents.md` file.
   * A brief `README.md` explaining:

     * What the app does.
     * Basic setup instructions.
     * How to configure the endpoint URL (even if currently hardcoded).
     * How to build locally (`make assembleDebug`, `make assembleRelease`).
     * How to view CI artifacts in GitHub Actions.

3. **Build tooling**

   * `Makefile` implementing the targets described above.
   * `.github/workflows/android-build.yml` workflow that builds the app and uploads APK/AAB artifacts.

4. **Code quality**

   * Clear, idiomatic Kotlin.
   * Minimal but meaningful comments.
   * Graceful error handling for storage and network operations.
