package com.juanitos.data.climbing.offline

import com.juanitos.data.climbing.daos.ClimbingWorkoutDao
import com.juanitos.data.climbing.entities.ClimbingWorkout
import com.juanitos.data.climbing.repositories.ClimbingWorkoutRepository
import kotlinx.coroutines.flow.Flow

class OfflineClimbingWorkoutRepository(
    private val climbingWorkoutDao: ClimbingWorkoutDao
) : ClimbingWorkoutRepository {
    override suspend fun insert(climbingWorkout: ClimbingWorkout): Long =
        climbingWorkoutDao.insert(climbingWorkout)

    override suspend fun update(climbingWorkout: ClimbingWorkout) =
        climbingWorkoutDao.update(climbingWorkout)

    override suspend fun delete(climbingWorkout: ClimbingWorkout) =
        climbingWorkoutDao.delete(climbingWorkout)

    override fun getAll(): Flow<List<ClimbingWorkout>> = climbingWorkoutDao.getAll()
}
