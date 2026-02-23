package com.juanitos.data.workout.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "workout_sets",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutExercise::class,
            parentColumns = ["id"],
            childColumns = ["workout_exercise_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("workout_exercise_id")]
)
data class WorkoutSet(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    @ColumnInfo(name = "workout_exercise_id")
    val workoutExerciseId: Int,
    val reps: Int? = null,
    @ColumnInfo(name = "duration_seconds")
    val durationSeconds: Int? = null,
    @ColumnInfo(name = "weight_kg")
    val weightKg: Double? = null,
    val position: Int = 0,
)
