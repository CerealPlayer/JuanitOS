package com.juanitos.data.workout.offline

import com.juanitos.data.workout.daos.WorkoutSetDao
import com.juanitos.data.workout.entities.WorkoutSet
import com.juanitos.data.workout.repositories.WorkoutSetRepository
import kotlinx.coroutines.flow.Flow

data class OfflineWorkoutSetRepository(
    private val workoutSetDao: WorkoutSetDao
) : WorkoutSetRepository {
    override suspend fun insert(workoutSet: WorkoutSet): Long = workoutSetDao.insert(workoutSet)
    override suspend fun update(workoutSet: WorkoutSet) = workoutSetDao.update(workoutSet)
    override suspend fun delete(workoutSet: WorkoutSet) = workoutSetDao.delete(workoutSet)
    override fun getByWorkoutExerciseId(workoutExerciseId: Int): Flow<List<WorkoutSet>> =
        workoutSetDao.getByWorkoutExerciseId(workoutExerciseId)
}
