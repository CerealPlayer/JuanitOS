package com.juanitos.data.food.repositories

import com.juanitos.data.food.entities.HealthStat
import kotlinx.coroutines.flow.Flow

interface HealthStatRepository {
    fun getLatestHealthStat(): Flow<HealthStat?>
    suspend fun insertHealthStat(weight: Float, factor: Int)
    suspend fun updateHealthStat(stat: HealthStat)
    suspend fun deleteHealthStat(stat: HealthStat)
}