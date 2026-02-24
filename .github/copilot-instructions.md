# JuanitOS Copilot Instructions

## Project Overview

**JuanitOS** is an Android personal management application built with Kotlin and Jetpack Compose.
Currently implements:

- **Money Module**: Manage income, transactions, categories, and fixed spending organized by cycles
    - Track spending across cycles with start/end dates
    - Categorize transactions and fixed spending
    - View money summary with custom chart visualization
- **Workout Module**: Log and manage workouts with exercises and sets
  - Define reusable exercise definitions
  - Create workouts with multiple exercises and sets per exercise
  - Track start/end times per workout
  - Swipe-to-dismiss for deleting exercises and workouts
- **Food Module**: Planned but not yet implemented (route stub exists)

The app uses Room database (schema version 21) with offline-first architecture and local-only data (
no backend).

---

## Architecture

### Layered Structure

```
data/
  ├── money/
  │   ├── daos/ (CategoryDao, CycleDao, FixedSpendingDao, TransactionDao)
  │   ├── entities/
  │   │   ├── Category, Cycle, Transaction, FixedSpending
  │   │   └── relations/ (CurrentCycleWithDetails, TransactionWithCategory, FixedSpendingWithCategory)
  │   ├── offline/ (OfflineCategoryRepository, OfflineCycleRepository, etc.)
  │   └── repositories/ (Repository interfaces)
  ├── workout/
  │   ├── daos/ (ExerciseDefinitionDao, WorkoutDao, WorkoutExerciseDao, WorkoutSetDao)
  │   ├── entities/
  │   │   ├── ExerciseDefinition, Workout, WorkoutExercise, WorkoutSet
  │   │   └── relations/ (WorkoutExerciseWithSets, WorkoutWithExercises)
  │   ├── offline/ (OfflineExerciseDefinitionRepository, OfflineWorkoutRepository, etc.)
  │   └── repositories/ (Repository interfaces)
  ├── migrations/
  │   ├── Migrations.kt (5 migrations: 9→10→11→12→13→14)
  │   └── WorkoutMigrations.kt (2 migrations: 19→20→21)
  ├── AppContainer.kt (DI interface - 4 Money + 4 Workout repositories)
  └── JuanitOSDatabase.kt (Room DB v21, 8 entities, 7 migrations)

ui/
  ├── routes/
  │   ├── HomeScreen.kt (Money summary with chart)
  │   ├── HomeViewModel.kt
  │   ├── money/
  │   │   ├── MoneyScreen.kt (Main money dashboard)
  │   │   ├── MoneyViewModel.kt
  │   │   ├── FixedSpendingCard.kt, TransactionCard.kt (Reusable cards)
  │   │   ├── transactions/ (NewTransactionScreen, ViewModel)
  │   │   ├── spendings/ (FixedSpendingsScreen, NewFixedSpendingScreen, ViewModels)
  │   │   ├── categories/ (CategoriesScreen, NewCategoryScreen, ViewModels)
  │   │   └── settings/ (MoneySettings, ViewModel)
  │   └── workout/
  │       ├── WorkoutScreen.kt (Workout list)
  │       ├── WorkoutViewModel.kt
  │       ├── WorkoutCard.kt (Reusable card with swipe-to-dismiss)
  │       ├── NewWorkoutScreen.kt (Create workout with exercises/sets)
  │       ├── NewWorkoutViewModel.kt
  │       └── exercises/
  │           ├── ExercisesScreen.kt (Exercise definitions list)
  │           ├── ExercisesViewModel.kt
  │           ├── NewExerciseScreen.kt
  │           └── NewExerciseViewModel.kt
  ├── navigation/
  │   ├── JuanitOSNavGraph.kt (Composable navigation)
  │   ├── JuanitOSTopAppBar.kt (Top app bar component)
  │   ├── Routes.kt (Enum with 13 route definitions)
  │   └── NavigationDestination.kt (Interface for typed destinations)
  ├── commons/
  │   ├── MoneySummaryChart.kt (Custom bar chart visualization)
  │   ├── DeleteConfirmationDialog.kt
  │   ├── FormColumn.kt
  │   ├── search/ (Generic Search composable)
  │   └── categories_search/ (CategoriesSearch composable)
  ├── icons/ (Add, ArrowBack, Delete, MoreVert, Search, Settings)
  ├── theme/ (Color, Theme, Type)
  └── AppViewModelProvider.kt (Factory with 12 ViewModels)

lib/
  ├── InputUiState.kt (Generic input state with value, touched, isValid)
  ├── dates.kt (Date utility functions)
  └── validation.kt (validateQtInt and other validators)
```

### Dependency Injection Pattern

- **AppContainer** interface defines all repositories (4 Money + 4 Workout repositories)
- **AppDataContainer** (in AppContainer.kt) implements with lazy-initialized repositories
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
// Routes.kt - Define route enum
enum class Routes(val route: String) {
    Home("home"),
    Money("money"),
    NewTransaction("new_transaction")
}

// Destination object with route and title
object MoneyDestination : NavigationDestination {
    override val route = Routes.Money.route
    override val titleRes = R.string.money_title
}

// JuanitOSNavGraph - Register composable routes
composable(Routes.Money.route) {
    MoneyScreen(onNavigateToSettings = { navController.navigate(Routes.MoneySettings.route) })
}

// ViewModels without parameters don't need SavedStateHandle
// If route parameters needed, extract from SavedStateHandle
```

### UI State Data Classes

Each screen has a dedicated UiState data class:

- Contains form inputs, validation states, error messages, loading flags
- Example: `NewTransactionUiState(amount: InputUiState, categoryId: Int?, ...)`
- For form inputs, use `InputUiState(value, touched, isValid)` from `lib/InputUiState.kt`
- Screens receive UiState via `.collectAsState()` from ViewModel
- Complex screens may have nested data classes like `MoneySummary` for aggregated data

---

## Database & Migrations

### Current Schema (version 21)

**Entities (4 Money + 4 Workout entities):**

- **Cycle** - Payment cycles with start/end dates and total income
- **Transaction** - One-time transactions linked to cycles and categories
- **FixedSpending** - Recurring spending with active/deleted state
- **Category** - Categories for organizing transactions and fixed spending
- **ExerciseDefinition** - Reusable exercise definitions (name, muscle group, etc.)
- **Workout** - A workout session with startTime and endTime (stored as epoch ms, formatted via
  `lib/dates.kt`)
- **WorkoutExercise** - An exercise instance within a workout (links Workout ↔ ExerciseDefinition)
- **WorkoutSet** - A set within a WorkoutExercise (reps, weight, etc.)

**Entity Relations (5):**

- **CurrentCycleWithDetails** - Cycle + List<TransactionWithCategory>
- **FixedSpendingWithCategory** - FixedSpending + Category
- **TransactionWithCategory** - Transaction + Category
- **WorkoutWithExercises** - Workout + List<WorkoutExerciseWithSets>
- **WorkoutExerciseWithSets** - WorkoutExercise + List<WorkoutSet>

**Note:** Food module entities mentioned in legacy migrations (foods, batch_foods, ingredients,
food_ingredients, batch_food_ingredients) but currently not in active schema.

### Migration Pattern

- Money migrations: `data/migrations/Migrations.kt` (MIGRATION_9_10 through MIGRATION_13_14)
- Workout migrations: `data/migrations/WorkoutMigrations.kt` (MIGRATION_19_20, MIGRATION_20_21)
- Use `fallbackToDestructiveMigration(false)` to prevent data loss
- New migrations must be added to `JuanitOSDatabase.addMigrations()`
- **Existing migrations cover Food module tables** that are not in current active schema (v14→v19
  gap indicates potential schema changes)

---

## Testing Notes

- Test directories exist but are empty (`src/test/`, `src/androidTest/`)
- Use AndroidJUnitRunner for instrumented tests (configured in build.gradle)
- No existing test infrastructure to follow

---

## Key Dependencies

- **Kotlin 2.3.0** with Compose compiler plugin
- **Jetpack Compose BOM 2026.01.00** for UI
- **Room 2.8.4** for database with KSP annotation processing
- **Navigation Compose 2.9.6** for typed navigation
- **KSP 2.3.4** for Room code generation
- **Material3 1.4.0** for design system
- **Lifecycle Runtime KTX 2.10.0** for coroutines and ViewModels
- **Activity Compose 1.12.2** for activity integration
- **Core KTX 1.17.0** for Android extensions
- **Vico 2.4.1** for charts (imported but not actively used; custom charts in MoneySummaryChart.kt)

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

## Current Implementation Scope

### Implemented Modules

**Money Module** (Complete)

- **8 Screens**: Home, Money, MoneySettings, NewTransaction, FixedSpendings, NewFixedSpending,
  Categories, NewCategory
- **4 Entities**: Cycle, Transaction, FixedSpending, Category
- **4 Repositories**: CycleRepository, TransactionRepository, FixedSpendingRepository,
  CategoryRepository
- **8 ViewModels**: All screens have dedicated ViewModels with StateFlow pattern
- **Features**:
    - Cycle management with start/end dates and income tracking
    - Transaction tracking with category assignment
    - Fixed spending with active/inactive state
    - Category management for organizing spending
    - Custom bar chart visualization (MoneySummaryChart)
    - Search functionality for categories
    - Delete confirmation dialogs

**Workout Module** (Complete)

- **4 Screens**: Workout (list), NewWorkout, Exercises (definitions list), NewExercise
- **4 Entities**: ExerciseDefinition, Workout, WorkoutExercise, WorkoutSet
- **4 Repositories**: ExerciseDefinitionRepository, WorkoutRepository, WorkoutExerciseRepository,
  WorkoutSetRepository
- **4 ViewModels**: WorkoutViewModel, NewWorkoutViewModel, ExercisesViewModel, NewExerciseViewModel
- **Features**:
  - Define reusable exercise definitions
  - Create workouts composed of multiple exercises with sets (reps/weight)
  - Track workout start/end time (epoch ms, formatted via `lib/dates.kt`)
  - Swipe-to-dismiss with delete confirmation for workouts and exercises
  - `WorkoutCard.kt` reusable card component

**Food Module** (Not Implemented)

- Route stub exists in `Routes.kt` but no screens, entities, or repositories implemented
- Legacy migration files reference food-related tables (foods, batch_foods, ingredients) but these
  are not in current schema

### Common Components

- **Custom Icons**: 6 Material icons (Add, ArrowBack, Delete, MoreVert, Search, Settings) in
  `ui/icons/`
- **Reusable Composables**:
    - `MoneySummaryChart.kt` - Custom bar chart with income/expenses/remaining
    - `DeleteConfirmationDialog.kt` - Confirmation dialog for deletions
    - `FormColumn.kt` - Form layout wrapper
    - `Search.kt` - Generic search component
    - `CategoriesSearch.kt` - Category-specific search
  - `TransactionCard.kt`, `FixedSpendingCard.kt` - Money list item cards
  - `WorkoutCard.kt` - Workout list item card with swipe-to-dismiss
- **Utilities**:
    - `lib/InputUiState.kt` - Generic input state (value, touched, isValid)
    - `lib/dates.kt` - Date formatting and manipulation
    - `lib/validation.kt` - Input validators

---

## Developer Workflows

### Adding a New Screen

1. Create screen composable in `ui/routes/{module}/{feature}/`
2. Define `{Screen}Destination` object with route from Routes enum and titleRes
3. Create `{Screen}ViewModel` injecting required repositories from AppContainer
4. Add ViewModel initializer to `AppViewModelProvider.Factory`
5. Create composable screen function in `{Screen}Screen.kt` (may include card components)
6. Add navigation entry in `JuanitOSNavGraph` with route from Routes enum
7. Wire navigation callback in parent screen

### Adding a New Entity

1. Create entity data class in `data/{module}/entities/` with Room annotations
2. Create DAO interface in `data/{module}/daos/` with @Dao annotation
3. Implement DAO methods with @Query, @Insert, @Update, @Delete annotations
4. Create repository interface in `data/{module}/repositories/` with Flow return types
5. Implement offline repository in `data/{module}/offline/` delegating to DAO
6. Add repository property to `AppContainer` interface
7. Implement lazy initialization in `AppDataContainer` class
8. Add entity to `JuanitOSDatabase @Database` entities array
9. Create abstract DAO getter in `JuanitOSDatabase`
10. Increment database version and create migration if needed

### Form Input Validation

- Use utility functions from `lib/validation.kt`: `validateQtInt()`, etc.
- Store validation state using `InputUiState(value, touched, isValid)` from `lib/InputUiState.kt`
- Validators should return Boolean; errors stored as optional String in UiState if needed
- Mark fields as `touched` when user interacts, validate only when touched

### Creating Reusable Card Components

- Create `{Entity}Card.kt` in same directory as screen for list items
- Example: `TransactionCard.kt`, `FixedSpendingCard.kt`
- Cards typically include entity display data and optional action callbacks (delete, edit)
- Used within LazyColumn in list screens

---

## Project-Specific Conventions

### Naming

- **Screen classes**: `{Feature}Screen.kt` with `@Composable` function and `{Feature}Destination`
  object
- **ViewModels**: `{Feature}ViewModel.kt` (e.g., `NewTransactionViewModel`,
  `MoneySettingsViewModel`)
- **UiState classes**: Defined in ViewModel file with `data class {Feature}UiState`
- **Card components**: `{Entity}Card.kt` for reusable list items (e.g., `TransactionCard.kt`)
- **Offline repositories**: Prefix `Offline` (e.g., `OfflineCycleRepository`)
- **DAOs**: Suffix `Dao` (e.g., `CycleDao`, `CategoryDao`)
- **Entity relations**: Suffix `With{RelatedEntity}` (e.g., `TransactionWithCategory`)

### Constants

- Use `companion object` in ViewModels for TIMEOUT_MILLIS (typically 5_000L)
- No magic numbers in repository logic; define in entity/DAO level

### Error Handling

- Validation errors stored in UiState.errorMessage (nullable String)
- No try-catch visible in ViewModels; assume repositories handle exceptions
- Use `IllegalArgumentException` for missing navigation parameters

---

## All Screens & Routes

| Route                       | Screen File                                | ViewModel                 | Purpose                                          |
|-----------------------------|--------------------------------------------|---------------------------|--------------------------------------------------|
| **home**                    | HomeScreen.kt                              | HomeViewModel             | Dashboard with money summary chart               |
| **food**                    | _(Not implemented)_                        | -                         | Placeholder route only                           |
| **money**                   | money/MoneyScreen.kt                       | MoneyViewModel            | Main money dashboard with transactions/spendings |
| **money_settings**          | money/settings/MoneySettings.kt            | MoneySettingsViewModel    | Cycle management settings                        |
| **new_transaction**         | money/transactions/NewTransactionScreen.kt | NewTransactionViewModel   | Create new transaction                           |
| **fixed_spending**          | money/spendings/FixedSpendingsScreen.kt    | FixedSpendingsViewModel   | List all fixed spendings                         |
| **new_fixed_spending**      | money/spendings/NewFixedSpendingScreen.kt  | NewFixedSpendingViewModel | Create new fixed spending                        |
| **categories**              | money/categories/CategoriesScreen.kt       | CategoriesViewModel       | List and manage categories                       |
| **new_category**            | money/categories/NewCategoryScreen.kt      | NewCategoryViewModel      | Create new category                              |
| **workout**                 | workout/WorkoutScreen.kt                   | WorkoutViewModel          | List all workouts with swipe-to-dismiss          |
| **new_workout**             | workout/NewWorkoutScreen.kt                | NewWorkoutViewModel       | Create workout with exercises and sets           |
| **exercises**               | workout/exercises/ExercisesScreen.kt       | ExercisesViewModel        | List exercise definitions                        |
| **new_exercise_definition** | workout/exercises/NewExerciseScreen.kt     | NewExerciseViewModel      | Create new exercise definition                   |

**Navigation Pattern**: All routes registered in `JuanitOSNavGraph.kt` using Routes enum values. No
parameterized routes currently in use.

---

## Common Gotchas

1. **SavedStateHandle for nav params**: Currently no routes use parameters; if adding parameterized
   routes, extract with null-coalescing and throw IllegalArgumentException if not found
2. **Flow vs StateFlow**: Queries return Flow, ViewModels convert to StateFlow with stateIn()
3. **Database migrations**: Must be added to JuanitOSDatabase.addMigrations() or they won't run
4. **Lazy repository initialization**: Use `by lazy { OfflineXRepository(...) }` in AppDataContainer
5. **Combine operator**: Order matters; final state is determined by last combine's copy()
6. **InputUiState usage**: Use for form fields instead of separate value/touched/isValid properties
7. **Food module status**: Route defined but implementation removed; migrations reference legacy
   food tables (v9-14) that are not in current active schema (v21)
8. **Workout migrations**: Stored in `WorkoutMigrations.kt` (separate from money migrations); covers
   v19→20→21 adding workout tables and startTime/endTime to Workout
9. **Workout time fields**: `Workout.startTime` and `Workout.endTime` are stored as epoch
   milliseconds (Long); use formatting functions from `lib/dates.kt` to display them

