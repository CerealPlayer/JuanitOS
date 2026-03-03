package com.juanitos.data.climbing.offline

import com.juanitos.data.climbing.daos.ClimbingBoulderAttemptDao
import com.juanitos.data.climbing.entities.ClimbingBoulderAttempt
import com.juanitos.data.climbing.repositories.ClimbingBoulderAttemptRepository
import kotlinx.coroutines.flow.Flow

class OfflineClimbingBoulderAttemptRepository(
    private val climbingBoulderAttemptDao: ClimbingBoulderAttemptDao
) : ClimbingBoulderAttemptRepository {
    override suspend fun insert(climbingBoulderAttempt: ClimbingBoulderAttempt): Long =
        climbingBoulderAttemptDao.insert(climbingBoulderAttempt)

    override suspend fun update(climbingBoulderAttempt: ClimbingBoulderAttempt) =
        climbingBoulderAttemptDao.update(climbingBoulderAttempt)

    override suspend fun delete(climbingBoulderAttempt: ClimbingBoulderAttempt) =
        climbingBoulderAttemptDao.delete(climbingBoulderAttempt)

    override fun getAll(): Flow<List<ClimbingBoulderAttempt>> = climbingBoulderAttemptDao.getAll()

    override fun getByClimbingWorkoutId(climbingWorkoutId: Int): Flow<List<ClimbingBoulderAttempt>> =
        climbingBoulderAttemptDao.getByClimbingWorkoutId(climbingWorkoutId)
}
