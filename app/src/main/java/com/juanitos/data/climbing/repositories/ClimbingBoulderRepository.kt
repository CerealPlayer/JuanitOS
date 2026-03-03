package com.juanitos.data.climbing.repositories

import com.juanitos.data.climbing.entities.ClimbingBoulder
import kotlinx.coroutines.flow.Flow

interface ClimbingBoulderRepository {
    suspend fun insert(climbingBoulder: ClimbingBoulder): Long
    suspend fun update(climbingBoulder: ClimbingBoulder)
    suspend fun delete(climbingBoulder: ClimbingBoulder)
    fun getAll(): Flow<List<ClimbingBoulder>>
}
