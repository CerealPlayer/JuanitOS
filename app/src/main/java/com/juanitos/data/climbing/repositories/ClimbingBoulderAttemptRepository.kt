package com.juanitos.data.climbing.repositories

import com.juanitos.data.climbing.entities.ClimbingBoulderAttempt
import kotlinx.coroutines.flow.Flow

interface ClimbingBoulderAttemptRepository {
    suspend fun insert(climbingBoulderAttempt: ClimbingBoulderAttempt): Long
    suspend fun update(climbingBoulderAttempt: ClimbingBoulderAttempt)
    suspend fun delete(climbingBoulderAttempt: ClimbingBoulderAttempt)
    fun getAll(): Flow<List<ClimbingBoulderAttempt>>
    fun getByClimbingWorkoutId(climbingWorkoutId: Int): Flow<List<ClimbingBoulderAttempt>>
}
