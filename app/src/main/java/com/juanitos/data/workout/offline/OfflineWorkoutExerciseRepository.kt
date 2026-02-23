package com.juanitos.data.workout.offline

import com.juanitos.data.workout.daos.WorkoutExerciseDao
import com.juanitos.data.workout.entities.WorkoutExercise
import com.juanitos.data.workout.entities.relations.WorkoutExerciseWithSets
import com.juanitos.data.workout.repositories.WorkoutExerciseRepository
import kotlinx.coroutines.flow.Flow

data class OfflineWorkoutExerciseRepository(
    private val workoutExerciseDao: WorkoutExerciseDao
) : WorkoutExerciseRepository {
    override suspend fun insert(workoutExercise: WorkoutExercise): Long =
        workoutExerciseDao.insert(workoutExercise)

    override suspend fun update(workoutExercise: WorkoutExercise) =
        workoutExerciseDao.update(workoutExercise)

    override suspend fun delete(workoutExercise: WorkoutExercise) =
        workoutExerciseDao.delete(workoutExercise)

    override fun getByIdWithSets(id: Int): Flow<WorkoutExerciseWithSets> =
        workoutExerciseDao.getByIdWithSets(id)

    override fun getByWorkoutIdWithSets(workoutId: Int): Flow<List<WorkoutExerciseWithSets>> =
        workoutExerciseDao.getByWorkoutIdWithSets(workoutId)
}
