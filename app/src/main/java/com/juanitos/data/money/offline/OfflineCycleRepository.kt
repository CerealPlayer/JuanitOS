package com.juanitos.data.money.offline

import com.juanitos.data.money.daos.CycleDao
import com.juanitos.data.money.entities.Cycle
import com.juanitos.data.money.entities.relations.CurrentCycleWithDetails
import com.juanitos.data.money.repositories.CycleRepository
import kotlinx.coroutines.flow.Flow

class OfflineCycleRepository(private val cycleDao: CycleDao) : CycleRepository {
    override suspend fun insert(income: Double): Long = cycleDao.insert(income)
    override suspend fun update(cycle: Cycle) = cycleDao.update(cycle)
    override suspend fun delete(cycle: Cycle) = cycleDao.delete(cycle)
    override fun getCurrentCycle(): Flow<CurrentCycleWithDetails?> = cycleDao.getCurrentCycle()
}
