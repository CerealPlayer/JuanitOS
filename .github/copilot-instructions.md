# JuanitOS Copilot Instructions

## Project Overview

**JuanitOS** is an Android personal management application built with Kotlin and Jetpack Compose. It
tracks two main domains:

- **Food Module**: Track daily food intake with ingredients, batch foods, and calorie management
- **Money Module**: Manage income, transactions, and fixed spending organized by cycles

The app uses Room database (schema version 16) with offline-first architecture and local-only data (
no backend).

---

## Architecture

### Layered Structure

```
data/
  ├── food/
  │   ├── daos/ (Room DAOs for each entity)
  │   ├── entities/ (Food, Ingredient, BatchFood, etc.)
  │   ├── offline/ (OfflineFoodRepository, etc.)
  │   └── repositories/ (Repository interfaces)
  ├── money/
  │   ├── daos/ (CycleDao, TransactionDao, FixedSpendingDao)
  │   ├── entities/ (Cycle, Transaction, FixedSpending)
  │   ├── offline/ (Offline implementations)
  │   └── repositories/ (Repository interfaces)
  ├── AppContainer.kt (Dependency injection interface)
  ├── AppDataContainer.kt (DI implementation with lazy initialization)
  └── JuanitOSDatabase.kt (Room database with 5 migrations: 9→10→11→12→13→14)

ui/
  ├── routes/
  │   ├── food/settings, batch, ingredients, track, new_food
  │   └── money/transactions, spendings, settings
  ├── navigation/
  │   ├── JuanitOSNavGraph.kt (Composable navigation)
  │   ├── Routes.kt (Enum with route definitions and parameter builders)
  │   └── NavigationDestination.kt (Interface for typed destinations)
  ├── AppViewModelProvider.kt (ViewModelFactory with all VM definitions)
  └── commons/ (Shared composables like FormColumn, QtDialog, Search)
```

### Dependency Injection Pattern

- **AppContainer** interface defines all repositories
- **AppDataContainer** implements with lazy-initialized repositories
- **AppViewModelProvider** uses viewModelFactory with `juanitOSApplication()` to access container
- All repositories are stateless, singleton instances

---

## Key Patterns

### ViewModel + StateFlow Pattern

All ViewModels follow this structure:

```kotlin
class MyViewModel(private val repository: MyRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(MyUiState())
    val uiState: StateFlow<MyUiState> = _uiState
        .combine(createDataFlow()) { state, data ->
            state.copy(data = data)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = MyUiState()
        )
}
```

- **5000ms timeout**: WhileSubscribed uses 5000ms grace period before canceling flows
- Use `_uiState.update { ... }` for state mutations
- Combine repository flows with state updates for derived UI state
- Always use `stateIn()` to convert flows to StateFlow

### Repository Pattern (Offline-Only)

- Each repository interface defines contract (synchronous queries via Flow)
- Offline implementation delegates to DAOs
- Use `suspend` functions for mutations, Flow for queries
- Example: `IngredientRepository.getAllIngredientsStream()` returns `Flow<List<Ingredient>>`

### Navigation with Typed Routes

```kotlin
// Routes.kt - Define parameter builders
fun createFoodDetailsRoute(foodId: Int) = "food/$foodId"

// Routes.kt - Navgraph uses typed enum and navArgument
navArgument("foodId") { type = NavType.IntType }

// ViewModels receive SavedStateHandle to extract params
val foodId = savedStateHandle.get<Int>("foodId") ?: throw IllegalArgumentException("foodId not found")
```

### UI State Data Classes

Each screen has a dedicated UiState data class:

- Contains form inputs, validation states, error messages, loading flags
- Example:
  `NewTransactionUiState(amountInput: String, isAmountValid: Boolean, currentCycleId: Int?, ...)`
- Screens receive UiState via `.collectAsState()` from ViewModel

---

## Database & Migrations

### Current Schema (version 16)

Entities: Setting, Food, FoodIngredient, Ingredient, BatchFood, BatchFoodIngredient, Cycle,
Transaction, FixedSpending

### Migration Pattern

- Store migrations in `data/migrations/` (MIGRATION_9_10, MIGRATION_10_11, etc.)
- Use `fallbackToDestructiveMigration(false)` to prevent data loss
- New migrations must be added to `JuanitOSDatabase.addMigrations()`

---

## Testing Notes

- Test directories exist but are empty (`src/test/`, `src/androidTest/`)
- Use AndroidJUnitRunner for instrumented tests (configured in build.gradle)
- No existing test infrastructure to follow

---

## Key Dependencies

- **Kotlin 2.3.0** with Compose compiler plugin
- **Jetpack Compose 2026.01.00** for UI
- **Room 2.8.4** for database with KSP annotation processing
- **Navigation Compose 2.9.6** for typed navigation
- **KSP 2.3.4** for Room code generation
- **Material3 1.4.0** for design system
- **Vico 2.4.1** for charts (imported but usage not visible in current structure)

---

## Build & Development

### Gradle Configuration

- `Android Gradle Plugin: 8.13.2`
- `Target SDK: 35`, `Min SDK: 24`, `Compile SDK: 36`
- `Java/Kotlin 11` source/target compatibility
- JVM args: `-Xmx2048m`
- Namespace: `com.juanitos`

### Build Variants

- **Debug**: Full app with UI tooling
- **Release**: Minification disabled (no ProGuard optimization)

### Custom Gradle Task Patterns

- None identified; uses standard Android Gradle tasks (build, assembleDebug, etc.)

---

## Developer Workflows

### Adding a New Screen

1. Create screen destination in `ui/routes/{module}/{screen}/`
2. Define `{Screen}Destination` with route and titleRes
3. Create `{Screen}ViewModel` injecting required repositories
4. Add ViewModel to `AppViewModelProvider.Factory`
5. Create composable screen function in `{Screen}Screen.kt`
6. Add navigation in `JuanitOSNavGraph` with route and arguments
7. Wire navigation callback in parent screen

### Adding a New Entity

1. Create entity data class in `data/{module}/entities/`
2. Create DAO interface in `data/{module}/daos/`
3. Implement DAO methods with @Query annotations
4. Create repository interface in `data/{module}/repositories/`
5. Implement offline repository in `data/{module}/offline/`
6. Add to `AppContainer` and `AppDataContainer`
7. Create migration if modifying schema

### Form Input Validation

- Use utility functions: `validateQtInt()`, custom validators in `lib/validation.kt`
- Store validation state in UiState (isAmountValid, isIncomeValid, etc.)
- Validators should return Boolean; errors stored as optional String in UiState

---

## Project-Specific Conventions

### Naming

- **Screen classes**: `{Feature}Screen.kt` with `@Composable` function and `{Feature}Destination`
  object
- **ViewModels**: `{Feature}ViewModel.kt` (e.g., `NewFoodViewModel`, `FoodSettingsViewModel`)
- **UiState classes**: Nested in ViewModel file or separate with `data class {Feature}UiState`
- **Offline repositories**: Prefix `Offline` (e.g., `OfflineFoodRepository`)
- **DAOs**: Suffix `Dao` (e.g., `FoodDao`, `IngredientDao`)

### Constants

- Use `companion object` in ViewModels for TIMEOUT_MILLIS (typically 5_000L)
- No magic numbers in repository logic; define in entity/DAO level

### Error Handling

- Validation errors stored in UiState.errorMessage (nullable String)
- No try-catch visible in ViewModels; assume repositories handle exceptions
- Use `IllegalArgumentException` for missing navigation parameters

---

## Common Gotchas

1. **SavedStateHandle for nav params**: Always extract with null-coalescing and throw
   IllegalArgumentException if not found
2. **Flow vs StateFlow**: Queries return Flow, ViewModels convert to StateFlow with stateIn()
3. **Database migrations**: Must be added to JuanitOSDatabase.addMigrations() or they won't run
4. **Lazy repository initialization**: Use `by lazy { OfflineXRepository(...) }` in AppDataContainer
5. **Combine operator**: Order matters; final state is determined by last combine's copy()

