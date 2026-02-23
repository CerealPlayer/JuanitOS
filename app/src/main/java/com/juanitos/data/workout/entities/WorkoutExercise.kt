package com.juanitos.data.workout.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "workout_exercises",
    foreignKeys = [
        ForeignKey(
            entity = Workout::class,
            parentColumns = ["id"],
            childColumns = ["workout_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ExerciseDefinition::class,
            parentColumns = ["id"],
            childColumns = ["exercise_definition_id"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("workout_id"), Index("exercise_definition_id")]
)
data class WorkoutExercise(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    @ColumnInfo(name = "workout_id")
    val workoutId: Int,
    @ColumnInfo(name = "exercise_definition_id")
    val exerciseDefinitionId: Int,
    val position: Int = 0,
)
