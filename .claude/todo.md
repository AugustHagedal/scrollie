# FrictionScroll MVP - Master TODO

**IMPORTANT: Always read this file at the start of every session.**

## Status: MVP COMPLETE

---

## Phase 1: Project Skeleton
- [x] Create root build.gradle.kts (project-level)
- [x] Create settings.gradle.kts
- [x] Create gradle.properties
- [x] Create app/build.gradle.kts with all dependencies
- [x] Create AndroidManifest.xml with all permissions + service declaration
- [x] Create Application class

## Phase 2: Data Layer
- [x] SettingsStore.kt — DataStore wrapper for all persisted state

## Phase 3: Engine
- [x] Clock.kt — interface for deterministic testing
- [x] FrictionEngine.kt — pure logic: rolling window, debounce, cooldown, snooze
- [x] FrictionEngineTest.kt — unit tests (9 tests, all passing)

## Phase 4: Service + Overlay
- [x] ScrollFrictionAccessibilityService.kt — AccessibilityEvent handling
- [x] OverlayController.kt — WindowManager overlay show/hide/countdown
- [x] PermissionHelper.kt — check overlay + accessibility permissions
- [x] accessibility_service_config.xml — service metadata

## Phase 5: UI Screens (Compose + Navigation)
- [x] MainActivity.kt + NavGraph
- [x] HomeScreen.kt — toggle, status, quick stats
- [x] AppPickerScreen.kt — installed apps list with search + checkboxes
- [x] BurstConfigScreen.kt — N, T, delay, cooldown sliders
- [x] PermissionsScreen.kt — checklist with deep-links
- [x] DebugScreen.kt — logs/stats viewer
- [x] Theme.kt

## Phase 6: Integration & Polish
- [x] Wire service <-> SettingsStore (Flow collection)
- [x] Wire overlay callbacks (disable, snooze)
- [x] Hilt DI module (AppModule.kt)

## Build Info
- AGP 8.7.3, Kotlin 2.0.21, Compose BOM 2024.10.00, Hilt 2.52
- Requires JDK 17: `JAVA_HOME=/opt/homebrew/opt/openjdk@17`
- Android SDK at: `/opt/homebrew/share/android-commandlinetools`
- Build: `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ANDROID_HOME=/opt/homebrew/share/android-commandlinetools ./gradlew assembleDebug`
- Tests: `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ANDROID_HOME=/opt/homebrew/share/android-commandlinetools ./gradlew :app:testDebugUnitTest`

---

## Key Design Decisions
- Single-module app (packages for separation, not Gradle modules)
- Hilt for DI
- DataStore Preferences (not Proto) for simplicity
- FrictionEngine is pure Kotlin, no Android deps, fully testable
- OverlayController uses WindowManager TYPE_APPLICATION_OVERLAY
- Service communicates with UI via SettingsStore Flows
