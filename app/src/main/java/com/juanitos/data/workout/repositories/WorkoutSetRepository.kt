package com.juanitos.data.workout.repositories

import com.juanitos.data.workout.entities.WorkoutSet
import kotlinx.coroutines.flow.Flow

interface WorkoutSetRepository {
    suspend fun insert(workoutSet: WorkoutSet): Long
    suspend fun update(workoutSet: WorkoutSet)
    suspend fun delete(workoutSet: WorkoutSet)
    fun getByWorkoutExerciseId(workoutExerciseId: Int): Flow<List<WorkoutSet>>
}
