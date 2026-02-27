package com.juanitos.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.juanitos.data.habit.daos.HabitDao
import com.juanitos.data.habit.daos.HabitEntryDao
import com.juanitos.data.habit.entities.Habit
import com.juanitos.data.habit.entities.HabitEntry
import com.juanitos.data.migrations.MIGRATION_10_11
import com.juanitos.data.migrations.MIGRATION_11_12
import com.juanitos.data.migrations.MIGRATION_12_13
import com.juanitos.data.migrations.MIGRATION_13_14
import com.juanitos.data.migrations.MIGRATION_19_20
import com.juanitos.data.migrations.MIGRATION_20_21
import com.juanitos.data.migrations.MIGRATION_21_22
import com.juanitos.data.migrations.MIGRATION_22_23
import com.juanitos.data.migrations.MIGRATION_9_10
import com.juanitos.data.money.daos.CategoryDao
import com.juanitos.data.money.daos.CycleDao
import com.juanitos.data.money.daos.FixedSpendingDao
import com.juanitos.data.money.daos.TransactionDao
import com.juanitos.data.money.entities.Category
import com.juanitos.data.money.entities.Cycle
import com.juanitos.data.money.entities.FixedSpending
import com.juanitos.data.money.entities.Transaction
import com.juanitos.data.workout.daos.ExerciseDefinitionDao
import com.juanitos.data.workout.daos.WorkoutDao
import com.juanitos.data.workout.daos.WorkoutExerciseDao
import com.juanitos.data.workout.daos.WorkoutSetDao
import com.juanitos.data.workout.entities.ExerciseDefinition
import com.juanitos.data.workout.entities.Workout
import com.juanitos.data.workout.entities.WorkoutExercise
import com.juanitos.data.workout.entities.WorkoutSet

@Database(
    entities = [
        Cycle::class, Transaction::class, FixedSpending::class, Category::class,
        ExerciseDefinition::class, Workout::class, WorkoutExercise::class, WorkoutSet::class,
        Habit::class, HabitEntry::class
    ],
    version = 23,
    exportSchema = false
)
abstract class JuanitOSDatabase : RoomDatabase() {
    abstract fun cycleDao(): CycleDao
    abstract fun transactionDao(): TransactionDao
    abstract fun fixedSpendingDao(): FixedSpendingDao
    abstract fun categoryDao(): CategoryDao
    abstract fun exerciseDefinitionDao(): ExerciseDefinitionDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun workoutExerciseDao(): WorkoutExerciseDao
    abstract fun workoutSetDao(): WorkoutSetDao
    abstract fun habitDao(): HabitDao
    abstract fun habitEntryDao(): HabitEntryDao

    companion object {
        @Volatile
        private var Instance: JuanitOSDatabase? = null

        fun getDatabase(context: Context): JuanitOSDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, JuanitOSDatabase::class.java, "JuanitOS_database")
                    .addMigrations(
                        MIGRATION_9_10,
                        MIGRATION_10_11,
                        MIGRATION_11_12,
                        MIGRATION_12_13,
                        MIGRATION_13_14,
                        MIGRATION_19_20,
                        MIGRATION_20_21,
                        MIGRATION_21_22,
                        MIGRATION_22_23
                    )
                    .fallbackToDestructiveMigration(false)
                    .build().also { Instance = it }
            }
        }
    }
}
