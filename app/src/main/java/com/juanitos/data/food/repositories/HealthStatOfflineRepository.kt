package com.juanitos.data.food.repositories

import com.juanitos.data.food.daos.HealthStatDao
import com.juanitos.data.food.entities.HealthStat
import kotlinx.coroutines.flow.Flow

class HealthStatOfflineRepository(private val healthStatDao: HealthStatDao) : HealthStatRepository {
    override fun getLatestHealthStat(): Flow<HealthStat?> = healthStatDao.getLatestStat()
    override suspend fun insertHealthStat(weight: Float, factor: Int) =
        healthStatDao.insert(weight, factor)

    override suspend fun updateHealthStat(stat: HealthStat) = healthStatDao.update(stat)
    override suspend fun deleteHealthStat(stat: HealthStat) = healthStatDao.delete(stat)
}