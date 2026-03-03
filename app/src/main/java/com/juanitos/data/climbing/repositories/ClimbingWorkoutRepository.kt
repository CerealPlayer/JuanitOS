package com.juanitos.data.climbing.repositories

import com.juanitos.data.climbing.entities.ClimbingWorkout
import kotlinx.coroutines.flow.Flow

interface ClimbingWorkoutRepository {
    suspend fun insert(climbingWorkout: ClimbingWorkout): Long
    suspend fun update(climbingWorkout: ClimbingWorkout)
    suspend fun delete(climbingWorkout: ClimbingWorkout)
    fun getAll(): Flow<List<ClimbingWorkout>>
}
