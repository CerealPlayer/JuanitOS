# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this
repository.

## Project Overview

**JuanitOS** is an Android personal management app built with Kotlin and Jetpack Compose. Modules:
Money, Workout, Habit, Climbing (all complete), and Food (route stub only, not implemented). Room
database schema v28, offline-first, no backend.

## Build Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Run unit tests
./gradlew test

# Run instrumented tests (requires connected device/emulator)
./gradlew connectedAndroidTest

# Run a single test class
./gradlew test --tests "com.juanitos.ExampleUnitTest"

# Clean build
./gradlew clean assembleDebug
```

## Architecture

### Layered Structure

```
app/src/main/java/com/juanitos/
  data/           # Room entities, DAOs, repositories, migrations
  ui/
    routes/       # Screen composables + ViewModels per module (money/, workout/, habit/, climbing/)
    navigation/   # JuanitOSNavGraph.kt, Routes.kt (23 routes), JuanitOSTopAppBar.kt
    commons/      # Shared composables (MoneySummaryChart, DeleteConfirmationDialog, FormColumn, Search)
    icons/        # Custom Material icon wrappers
    theme/        # Color, Theme, Type
    AppViewModelProvider.kt  # Factory for all 23 ViewModels
  lib/            # Utilities: InputUiState.kt, dates.kt, validation.kt
```

### Dependency Injection

Manual DI via `AppContainer` interface + `AppDataContainer` implementation (lazy-initialized
singleton repositories). ViewModels access the container through `AppViewModelProvider` using
`juanitOSApplication()`.

### ViewModel Pattern

All ViewModels use `MutableStateFlow` + `combine` + `stateIn(WhileSubscribed(5_000L))`. Mutations go
through `_uiState.update { ... }`. Form screens use `InputUiState(value, touched, isValid)` from
`lib/InputUiState.kt`.

### Navigation

Routes are defined in the `Routes` enum (`Routes.kt`). Each screen has a companion
`{Screen}Destination` object implementing `NavigationDestination`. Routes are registered in
`JuanitOSNavGraph.kt`. Parameterized routes (e.g., `workout_detail/{workoutId}`) extract IDs via
`SavedStateHandle`; throw `IllegalArgumentException` if missing.

### Repository Pattern

Repository interfaces return `Flow` for queries and `suspend` functions for mutations. Offline
implementations (`Offline{X}Repository`) delegate directly to DAOs.

## Database

- **Room v28**, 14 entities, 14 migrations
- Migration files: `data/migrations/Migrations.kt` (v9–14), `WorkoutMigrations.kt` (v19–21),
  `HabitMigrations.kt` (v21–23), `ClimbingMigrations.kt` (v23–28)
- New migrations must be registered in `JuanitOSDatabase.addMigrations()`
- `fallbackToDestructiveMigration(false)` — never drop migrations
- Workout start/end times are stored as nullable text columns (`start_time`, `end_time`) and
  formatted via `lib/dates.kt`

## Key Conventions

- **Naming**: `{Feature}Screen.kt`, `{Feature}ViewModel.kt`, `{Feature}UiState` (defined in
  ViewModel file), `Offline{X}Repository`, `{X}Dao`, `{Entity}With{Related}` for relations
- **Card components**: `{Entity}Card.kt` in the same directory as the screen, used inside
  `LazyColumn`
- **Constants**: `companion object { const val TIMEOUT_MILLIS = 5_000L }` in ViewModels
- **Validation**: `lib/validation.kt` validators return `Boolean`; errors are nullable `String` in
  UiState; validate only when field is `touched`

## Workflow: Adding a New Screen

1. Create composable in `ui/routes/{module}/{feature}/{Feature}Screen.kt` with a
   `{Feature}Destination` object
2. Create `{Feature}ViewModel` injecting repositories from `AppContainer`
3. Add ViewModel initializer to `AppViewModelProvider.Factory`
4. Register route in `JuanitOSNavGraph.kt`
5. Wire navigation callback in the parent screen

## Workflow: Adding a New Entity

1. Create entity in `data/{module}/entities/` with Room annotations
2. Create DAO in `data/{module}/daos/`
3. Create repository interface in `data/{module}/repositories/` (Flow for queries, suspend for
   writes)
4. Implement in `data/{module}/offline/Offline{X}Repository.kt`
5. Add to `AppContainer` interface and lazy-initialize in `AppDataContainer`
6. Add to `@Database` entities array in `JuanitOSDatabase`, add abstract DAO getter
7. Increment version, write migration, register it in `addMigrations()`

## Common Gotchas

- **Combine order matters**: the last `combine` call's `copy()` wins for final state
- **Food module**: route exists, no implementation; legacy migration files (v9–14) reference food
  tables not in current schema
- **Climbing attempt ordering**: always preserve `boulder_order` and `attempt_order` when writing
  attempts
- **Habit lifecycle**: `Habit.completedAt` tracks lifecycle; `HabitEntry` tracks per-day completions
- **Vico charts**: imported (`vico.compose.m3`) but unused; custom chart is `MoneySummaryChart.kt`
