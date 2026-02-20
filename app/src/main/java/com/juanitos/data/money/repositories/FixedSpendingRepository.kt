package com.juanitos.data.money.repositories

import com.juanitos.data.money.entities.FixedSpending
import com.juanitos.data.money.entities.relations.FixedSpendingWithCategory
import kotlinx.coroutines.flow.Flow

interface FixedSpendingRepository {
    suspend fun insert(fixedSpending: FixedSpending): Long
    suspend fun update(fixedSpending: FixedSpending)
    suspend fun delete(fixedSpending: FixedSpending)
    fun getById(id: Int): Flow<FixedSpendingWithCategory>
    fun getAll(): Flow<List<FixedSpendingWithCategory>>
    suspend fun updateEnabled(spendingId: Int, enabled: Boolean)
}
