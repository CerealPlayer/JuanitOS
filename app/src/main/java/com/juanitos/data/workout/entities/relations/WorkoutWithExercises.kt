package com.juanitos.data.workout.entities.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.juanitos.data.workout.entities.Workout
import com.juanitos.data.workout.entities.WorkoutExercise

data class WorkoutWithExercises(
    @Embedded val workout: Workout,
    @Relation(
        parentColumn = "id",
        entityColumn = "workout_id",
        entity = WorkoutExercise::class
    )
    val exercises: List<WorkoutExerciseWithSets>,
)
