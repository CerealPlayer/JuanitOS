package com.juanitos.data

import android.content.Context
import com.juanitos.data.money.offline.OfflineCategoryRepository
import com.juanitos.data.money.offline.OfflineCycleRepository
import com.juanitos.data.money.offline.OfflineFixedSpendingRepository
import com.juanitos.data.money.offline.OfflineTransactionRepository
import com.juanitos.data.money.repositories.CategoryRepository
import com.juanitos.data.money.repositories.CycleRepository
import com.juanitos.data.money.repositories.FixedSpendingRepository
import com.juanitos.data.money.repositories.TransactionRepository
import com.juanitos.data.workout.offline.OfflineExerciseDefinitionRepository
import com.juanitos.data.workout.offline.OfflineWorkoutExerciseRepository
import com.juanitos.data.workout.offline.OfflineWorkoutRepository
import com.juanitos.data.workout.offline.OfflineWorkoutSetRepository
import com.juanitos.data.workout.repositories.ExerciseDefinitionRepository
import com.juanitos.data.workout.repositories.WorkoutExerciseRepository
import com.juanitos.data.workout.repositories.WorkoutRepository
import com.juanitos.data.workout.repositories.WorkoutSetRepository

interface AppContainer {
    val cycleRepository: CycleRepository
    val transactionRepository: TransactionRepository
    val fixedSpendingRepository: FixedSpendingRepository
    val categoryRepository: CategoryRepository
    val exerciseDefinitionRepository: ExerciseDefinitionRepository
    val workoutRepository: WorkoutRepository
    val workoutExerciseRepository: WorkoutExerciseRepository
    val workoutSetRepository: WorkoutSetRepository
}

class AppDataContainer(private val context: Context) : AppContainer {
    override val cycleRepository: CycleRepository by lazy {
        OfflineCycleRepository(cycleDao = JuanitOSDatabase.getDatabase(context).cycleDao())
    }
    override val transactionRepository: TransactionRepository by lazy {
        OfflineTransactionRepository(
            transactionDao = JuanitOSDatabase.getDatabase(context).transactionDao()
        )
    }
    override val fixedSpendingRepository: FixedSpendingRepository by lazy {
        OfflineFixedSpendingRepository(
            fixedSpendingDao = JuanitOSDatabase.getDatabase(context).fixedSpendingDao()
        )
    }
    override val categoryRepository: CategoryRepository by lazy {
        OfflineCategoryRepository(
            categoryDao = JuanitOSDatabase.getDatabase(context).categoryDao()
        )
    }
    override val exerciseDefinitionRepository: ExerciseDefinitionRepository by lazy {
        OfflineExerciseDefinitionRepository(
            exerciseDefinitionDao = JuanitOSDatabase.getDatabase(context).exerciseDefinitionDao()
        )
    }
    override val workoutRepository: WorkoutRepository by lazy {
        OfflineWorkoutRepository(
            workoutDao = JuanitOSDatabase.getDatabase(context).workoutDao()
        )
    }
    override val workoutExerciseRepository: WorkoutExerciseRepository by lazy {
        OfflineWorkoutExerciseRepository(
            workoutExerciseDao = JuanitOSDatabase.getDatabase(context).workoutExerciseDao()
        )
    }
    override val workoutSetRepository: WorkoutSetRepository by lazy {
        OfflineWorkoutSetRepository(
            workoutSetDao = JuanitOSDatabase.getDatabase(context).workoutSetDao()
        )
    }
}
