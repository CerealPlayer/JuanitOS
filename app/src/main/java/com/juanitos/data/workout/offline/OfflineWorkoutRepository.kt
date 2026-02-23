package com.juanitos.data.workout.offline

import com.juanitos.data.workout.daos.WorkoutDao
import com.juanitos.data.workout.entities.Workout
import com.juanitos.data.workout.entities.relations.WorkoutWithExercises
import com.juanitos.data.workout.repositories.WorkoutRepository
import kotlinx.coroutines.flow.Flow

data class OfflineWorkoutRepository(
    private val workoutDao: WorkoutDao
) : WorkoutRepository {
    override suspend fun insert(workout: Workout): Long = workoutDao.insert(workout)
    override suspend fun update(workout: Workout) = workoutDao.update(workout)
    override suspend fun delete(workout: Workout) = workoutDao.delete(workout)
    override fun getById(id: Int): Flow<Workout> = workoutDao.getById(id)
    override fun getByIdWithExercises(id: Int): Flow<WorkoutWithExercises> =
        workoutDao.getByIdWithExercises(id)

    override fun getAll(): Flow<List<Workout>> = workoutDao.getAll()
    override fun getAllWithExercises(): Flow<List<WorkoutWithExercises>> =
        workoutDao.getAllWithExercises()
}
