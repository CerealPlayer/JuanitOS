package com.juanitos.data.workout.repositories

import com.juanitos.data.workout.entities.WorkoutExercise
import com.juanitos.data.workout.entities.relations.WorkoutExerciseWithSets
import kotlinx.coroutines.flow.Flow

interface WorkoutExerciseRepository {
    suspend fun insert(workoutExercise: WorkoutExercise): Long
    suspend fun update(workoutExercise: WorkoutExercise)
    suspend fun delete(workoutExercise: WorkoutExercise)
    fun getByIdWithSets(id: Int): Flow<WorkoutExerciseWithSets>
    fun getByWorkoutIdWithSets(workoutId: Int): Flow<List<WorkoutExerciseWithSets>>
}
