# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview
FrictionScroll is an Android productivity app (Kotlin + Jetpack Compose) that adds "scroll friction" to user-selected apps. It detects rapid scrolling via AccessibilityService and shows a full-screen overlay delay to break doomscrolling habits.

## IMPORTANT: Read the TODO list
**Always read `.claude/todo.md` at the start of every session.** It contains the master task list and current progress for the MVP build.

## Build Commands
```bash
./gradlew assembleDebug          # Build debug APK
./gradlew :app:testDebugUnitTest # Run unit tests
./gradlew clean                  # Clean build
```

## Architecture
Single-module app with package-based separation:
- `data/` — `SettingsStore` (DataStore Preferences wrapper, all persisted state)
- `engine/` — `FrictionEngine` (pure Kotlin logic, no Android deps) + `Clock` interface
- `service/` — `ScrollFrictionAccessibilityService` + `OverlayController` (WindowManager)
- `util/` — `PermissionHelper`
- `ui/` — Compose screens (Home, AppPicker, BurstConfig, Permissions, Debug) + theme + navigation
- `di/` — Hilt module

## Key Patterns
- **DI**: Hilt. Service uses `EntryPointAccessors` (not constructor injection).
- **Persistence**: DataStore Preferences only (no SharedPreferences, no Room).
- **Engine testability**: `FrictionEngine` takes a `Clock` interface. Tests use `FakeClock`.
- **Service ↔ UI communication**: via DataStore Flows (no EventBus, no broadcasts).
- **Min SDK 26**, target latest stable.

## Constraints
- No analytics, no network calls
- No use of "accessibility" in user-facing copy — this is a "productivity/friction" tool
- Overlay must block touches during delay, auto-dismiss, and include emergency disable + snooze buttons
- Scroll event debounce: 150ms constant
