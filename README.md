# Stretchie Android App

## Overview
**Stretchie** is an Android application written in Kotlin that guides users through stretching routines. It uses Jetpack Compose for the UI, MVVM architecture, and Proto DataStore for persisting settings.

---

## Prerequisites
1. **Android Studio Flamingo (or newer)** – includes the required Android Gradle Plugin version `9.4.0`.
2. **JDK 17+** – Gradle is configured to use Java 17.
3. **Android SDK** – Ensure the following SDK platforms are installed via *SDK Manager*:
   - API 33 (Android 13)
   - API 36 (Android 14) – the `compileSdk` version.
4. **Emulator (optional)** – Create a virtual device with at least API 33.

---

## Project Setup
- **Sync Gradle** – Android Studio will automatically prompt to *Sync Project with Gradle Files*; click the **Sync** button (or use **File → Sync Project with Gradle Files**).
- **Verify the package name** – The `app/build.gradle.kts` file contains:
   ```kotlin
   namespace = "com.stretchie"
   applicationId = "com.stretchie"
   ```
   Adjust if you need a different namespace.

### Building & Running the App
1. **Select a run configuration** – In the toolbar, choose **app** as the module.
2. **Choose a device** – Either connect a physical Android device (enable USB debugging) or select an emulator from the device dropdown.
3. **Run** – Click the **Run** (green triangle) button. Android Studio will build the APK, install it on the selected device, and launch the app.
4. **Verify** – You should see a simple "Hello World" screen (the placeholder UI) confirming the scaffold works.

---

## Testing
### Unit Tests
- Located under `app/src/test/java/...`
- To run all unit tests: **Run → Run 'All Tests'** or use the terminal:
  ```bash
  ./gradlew test
  ```

### Instrumented Android Tests
- Located under `app/src/androidTest/java/...`
- To execute on an emulator/device:
  ```bash
  ./gradlew connectedAndroidTest
  ```
- Results appear in **Run → Android Tests** window.

---

## License

[MIT](LICENSE)
