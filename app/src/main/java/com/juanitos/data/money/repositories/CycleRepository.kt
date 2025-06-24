package com.juanitos.data.money.repositories

import com.juanitos.data.money.entities.Cycle
import com.juanitos.data.money.entities.relations.CurrentCycleWithDetails
import kotlinx.coroutines.flow.Flow

interface CycleRepository {
    suspend fun insert(income: Double): Long
    suspend fun update(cycle: Cycle)
    suspend fun delete(cycle: Cycle)
    fun getCurrentCycle(): Flow<CurrentCycleWithDetails?>
    suspend fun endCycle(cycleId: Int)
}
