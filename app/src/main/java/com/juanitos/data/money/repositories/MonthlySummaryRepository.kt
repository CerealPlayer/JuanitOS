package com.juanitos.data.money.repositories

import com.juanitos.data.money.entities.MonthlySummary
import kotlinx.coroutines.flow.Flow

interface MonthlySummaryRepository {
    fun getByAccount(accountId: Int): Flow<List<MonthlySummary>>
    suspend fun getByAccountAndMonth(accountId: Int, month: String): MonthlySummary?
    suspend fun finalizeElapsedMonths(accountId: Int)
}
