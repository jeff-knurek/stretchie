# Stretchie: Agent Guidelines

Welcome, AI Agent! If you're working on the Stretchie codebase, here is the essential context and rules to help you work efficiently.

## Project Architecture
- **Tech Stack**: Kotlin, Jetpack Compose, ViewModels (MVVM).
- **Data Persistence**: Android Jetpack Proto DataStore (`SettingsRepository.kt`).
- **Design Tokens**: Standardized within `app/src/main/java/com/stretchie/ui/theme/Theme.kt` with a custom gradient background. The UI design references are specified in a Pencil file (`stretchie-android.pen`).

## Core Business Logic
- `RoutineViewModel`: Manages the timer and handles advancing between poses. State transitions are controlled via a Kotlin Coroutine with `delay(1000)` ticks.
- `PoseRepository`: A hardcoded provider of `Pose` objects. 
- `SettingsRepository`: Manages user overrides such as skipped poses, custom durations, and sound selections via Protobuf DataStore.
- `AudioManager`: Interface for playing system sounds (alarms and notifications) on timer events using Android's `RingtoneManager`.

## Testing Best Practices
- **Framework**: We use JUnit 5 (`junit-jupiter-api`), `kotlinx-coroutines-test`, and `mockito-kotlin`.
- **Location**: All unit tests are located in `app/src/test/java/com/stretchie/`.
- **Running Tests**: Run tests via gradle using `./gradlew test`.
- **Coroutines Testing**: When testing `RoutineViewModel` or any other coroutine-based component, use `kotlinx.coroutines.test.StandardTestDispatcher()` and inject it via `Dispatchers.setMain(...)` to manipulate time using `advanceTimeBy()`.
- **Mocking**: For dependencies relying on Android Context (e.g. `AudioManager` and `SettingsRepository`), mock them using Mockito's `mock()` rather than running Robolectric.

## General Development Rules
- **No Remainder on Intervals**: When calculating intervals, we use standard integer division and drop the remainder.
- **Extendability**: Make sure new static poses are added correctly inside the `PoseRepository` list and their corresponding resources are placed in the `res/drawable/` directory.

## Getting Started
Run `./gradlew connectedAndroidTest` to run instrumented tests and `./gradlew test` for unit testing. For a quick UI check, deploy directly using standard Android Studio run configurations.
