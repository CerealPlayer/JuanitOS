# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this
repository.

## Project Overview

**JuanitOS** is an Android personal finance app built with Kotlin and Jetpack Compose. It was
previously a multi-module personal management app (Money, Workout, Habit, Climbing, Food stub);
all non-money modules were removed and the app is now Money-only. Room database schema v29,
offline-first, no backend.

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
  data/
    money/          # Room entities, DAOs, repositories, offline impls (the only data module)
    migrations/      # Migrations.kt (legacy v9-14), CleanupMigrations.kt (v28-29 drops removed modules' tables)
    AppContainer.kt  # AppContainer interface + AppDataContainer impl
    JuanitOSDatabase.kt
  ui/
    routes/money/   # Screen composables + ViewModels (categories/, spendings/, stats/, settings/, transactions/)
    navigation/     # JuanitOSNavGraph.kt, Routes.kt (8 routes), JuanitOSTopAppBar.kt
    commons/        # Shared composables (MoneySummaryChart, DeleteConfirmationDialog, FormColumn, Search, CategoriesSearch)
    icons/          # Custom Material icon wrappers
    theme/          # Color, Theme, Type
    AppViewModelProvider.kt  # Factory for the 8 Money ViewModels
  lib/            # Utilities: InputUiState.kt, dates.kt, validation.kt
```

The app opens directly into `MoneyScreen` (`MoneyDestination` is the nav graph's start
destination) — there is no more home/module-picker screen.

### Dependency Injection

Manual DI via `AppContainer` interface + `AppDataContainer` implementation (lazy-initialized
singleton repositories). ViewModels access the container through `AppViewModelProvider` using
`juanitOSApplication()`.

### ViewModel Pattern

All ViewModels use `MutableStateFlow` + `combine` + `stateIn(WhileSubscribed(5_000L))`. Mutations go
through `_uiState.update { ... }`. Form screens use `InputUiState(value, touched, isValid)` from
`lib/InputUiState.kt`.

### Navigation

Routes are defined in the `Routes` enum (`Routes.kt`): `Money`, `MoneyStats`, `MoneySettings`,
`NewTransaction`, `FixedSpending`, `NewFixedSpending`, `Categories`, `NewCategory`. Each screen has
a companion `{Screen}Destination` object implementing `NavigationDestination`. Routes are
registered in `JuanitOSNavGraph.kt`.

### Repository Pattern

Repository interfaces return `Flow` for queries and `suspend` functions for mutations. Offline
implementations (`Offline{X}Repository`) delegate directly to DAOs.

## Database

- **Room v29**, 4 entities (`Cycle`, `Transaction`, `FixedSpending`, `Category`), all in
  `data/money/`
- Migration files: `data/migrations/Migrations.kt` (v9–14, legacy — predates and does not apply to
  the current schema), `data/migrations/CleanupMigrations.kt` (`MIGRATION_28_29`, drops the tables
  that belonged to the removed Workout/Habit/Climbing modules)
- New migrations must be registered in `JuanitOSDatabase.addMigrations()`
- `fallbackToDestructiveMigration(false)` — never drop migrations

## Key Conventions

- **Naming**: `{Feature}Screen.kt`, `{Feature}ViewModel.kt`, `{Feature}UiState` (defined in
  ViewModel file), `Offline{X}Repository`, `{X}Dao`, `{Entity}With{Related}` for relations
- **Card components**: `{Entity}Card.kt` in the same directory as the screen, used inside
  `LazyColumn`
- **Constants**: `companion object { const val TIMEOUT_MILLIS = 5_000L }` in ViewModels
- **Validation**: `lib/validation.kt` validators return `Boolean`; errors are nullable `String` in
  UiState; validate only when field is `touched`

## Workflow: Adding a New Screen

1. Create composable in `ui/routes/money/{feature}/{Feature}Screen.kt` with a
   `{Feature}Destination` object
2. Create `{Feature}ViewModel` injecting repositories from `AppContainer`
3. Add ViewModel initializer to `AppViewModelProvider.Factory`
4. Register route in `JuanitOSNavGraph.kt`
5. Wire navigation callback in the parent screen

## Workflow: Adding a New Entity

1. Create entity in `data/money/entities/` with Room annotations
2. Create DAO in `data/money/daos/`
3. Create repository interface in `data/money/repositories/` (Flow for queries, suspend for
   writes)
4. Implement in `data/money/offline/Offline{X}Repository.kt`
5. Add to `AppContainer` interface and lazy-initialize in `AppDataContainer`
6. Add to `@Database` entities array in `JuanitOSDatabase`, add abstract DAO getter
7. Increment version, write migration, register it in `addMigrations()`

## Common Gotchas

- **Combine order matters**: the last `combine` call's `copy()` wins for final state
- **`data/migrations/Migrations.kt`**: legacy pre-money-only migrations (v9–14) referencing food
  tables that never existed in this schema — dead weight kept only for migration-chain continuity;
  do not model new migrations on it
- **Vico charts**: imported (`vico.compose.m3`) but unused; custom chart is `MoneySummaryChart.kt`
