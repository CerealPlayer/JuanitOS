package com.juanitos.data.workout.entities.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.juanitos.data.workout.entities.ExerciseDefinition
import com.juanitos.data.workout.entities.WorkoutExercise
import com.juanitos.data.workout.entities.WorkoutSet

data class WorkoutExerciseWithSets(
    @Embedded val workoutExercise: WorkoutExercise,
    @Relation(
        parentColumn = "exercise_definition_id",
        entityColumn = "id"
    )
    val exerciseDefinition: ExerciseDefinition,
    @Relation(
        parentColumn = "id",
        entityColumn = "workout_exercise_id"
    )
    val sets: List<WorkoutSet>,
)
