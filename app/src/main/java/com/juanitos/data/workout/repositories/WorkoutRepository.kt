package com.juanitos.data.workout.repositories

import com.juanitos.data.workout.entities.Workout
import com.juanitos.data.workout.entities.relations.WorkoutWithExercises
import kotlinx.coroutines.flow.Flow

interface WorkoutRepository {
    suspend fun insert(workout: Workout): Long
    suspend fun update(workout: Workout)
    suspend fun delete(workout: Workout)
    fun getById(id: Int): Flow<Workout>
    fun getByIdWithExercises(id: Int): Flow<WorkoutWithExercises?>
    fun getAll(): Flow<List<Workout>>
    fun getAllWithExercises(): Flow<List<WorkoutWithExercises>>
}
