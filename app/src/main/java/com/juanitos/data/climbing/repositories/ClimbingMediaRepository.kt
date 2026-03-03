package com.juanitos.data.climbing.repositories

import com.juanitos.data.climbing.entities.ClimbingMedia
import kotlinx.coroutines.flow.Flow

interface ClimbingMediaRepository {
    suspend fun insert(climbingMedia: ClimbingMedia): Long
    suspend fun update(climbingMedia: ClimbingMedia)
    suspend fun delete(climbingMedia: ClimbingMedia)
    fun getAll(): Flow<List<ClimbingMedia>>
}
