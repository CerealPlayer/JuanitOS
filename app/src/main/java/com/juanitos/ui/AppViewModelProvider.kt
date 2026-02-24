package com.juanitos.ui

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.juanitos.JuanitOSApplication
import com.juanitos.ui.routes.HomeViewModel
import com.juanitos.ui.routes.money.MoneyViewModel
import com.juanitos.ui.routes.money.categories.CategoriesViewModel
import com.juanitos.ui.routes.money.categories.NewCategoryViewModel
import com.juanitos.ui.routes.money.spendings.FixedSpendingsViewModel
import com.juanitos.ui.routes.money.spendings.NewFixedSpendingViewModel
import com.juanitos.ui.routes.money.transactions.NewTransactionViewModel
import com.juanitos.ui.routes.workout.NewWorkoutViewModel
import com.juanitos.ui.routes.workout.WorkoutViewModel
import com.juanitos.ui.routes.workout.detail.WorkoutDetailViewModel
import com.juanitos.ui.routes.workout.edit.EditWorkoutViewModel
import com.juanitos.ui.routes.workout.exercises.ExercisesViewModel
import com.juanitos.ui.routes.workout.exercises.NewExerciseViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            HomeViewModel(
                juanitOSApplication().container.cycleRepository,
                juanitOSApplication().container.fixedSpendingRepository,
            )
        }
        initializer {
            MoneyViewModel(
                juanitOSApplication().container.cycleRepository,
                juanitOSApplication().container.fixedSpendingRepository,
                juanitOSApplication().container.transactionRepository
            )
        }
        initializer {
            com.juanitos.ui.routes.money.settings.MoneySettingsViewModel(
                juanitOSApplication().container.cycleRepository
            )
        }
        initializer {
            NewTransactionViewModel(
                juanitOSApplication().container.transactionRepository,
                juanitOSApplication().container.cycleRepository,
                juanitOSApplication().container.categoryRepository
            )
        }
        initializer {
            FixedSpendingsViewModel(
                juanitOSApplication().container.fixedSpendingRepository
            )
        }
        initializer {
            NewFixedSpendingViewModel(
                juanitOSApplication().container.fixedSpendingRepository,
                juanitOSApplication().container.categoryRepository
            )
        }
        initializer {
            CategoriesViewModel(
                juanitOSApplication().container.categoryRepository
            )
        }
        initializer {
            NewCategoryViewModel(
                juanitOSApplication().container.categoryRepository
            )
        }
        initializer {
            WorkoutViewModel(
                juanitOSApplication().container.workoutRepository
            )
        }
        initializer {
            NewWorkoutViewModel(
                juanitOSApplication().container.workoutRepository,
                juanitOSApplication().container.workoutExerciseRepository,
                juanitOSApplication().container.workoutSetRepository,
                juanitOSApplication().container.exerciseDefinitionRepository
            )
        }
        initializer {
            WorkoutDetailViewModel(
                savedStateHandle = createSavedStateHandle(),
                workoutRepository = juanitOSApplication().container.workoutRepository,
            )
        }
        initializer {
            EditWorkoutViewModel(
                savedStateHandle = createSavedStateHandle(),
                workoutRepository = juanitOSApplication().container.workoutRepository,
                workoutExerciseRepository = juanitOSApplication().container.workoutExerciseRepository,
                workoutSetRepository = juanitOSApplication().container.workoutSetRepository,
                exerciseDefinitionRepository = juanitOSApplication().container.exerciseDefinitionRepository
            )
        }
        initializer {
            ExercisesViewModel(
                juanitOSApplication().container.exerciseDefinitionRepository
            )
        }
        initializer {
            NewExerciseViewModel(
                juanitOSApplication().container.exerciseDefinitionRepository
            )
        }
    }
}

fun CreationExtras.juanitOSApplication(): JuanitOSApplication =
    (this[AndroidViewModelFactory.APPLICATION_KEY] as JuanitOSApplication)
