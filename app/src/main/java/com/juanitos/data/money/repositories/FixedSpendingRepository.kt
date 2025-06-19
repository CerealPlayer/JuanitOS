package com.juanitos.data.money.repositories

import com.juanitos.data.money.entities.FixedSpending
import kotlinx.coroutines.flow.Flow

interface FixedSpendingRepository {
    suspend fun insert(fixedSpending: FixedSpending)
    suspend fun update(fixedSpending: FixedSpending)
    suspend fun delete(fixedSpending: FixedSpending)
    fun getById(id: Int): Flow<FixedSpending>
}
