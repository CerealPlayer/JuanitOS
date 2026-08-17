package com.juanitos.data.money.repositories

import com.juanitos.data.money.entities.IncomeSchedule
import kotlinx.coroutines.flow.Flow

interface IncomeScheduleRepository {
    suspend fun upsert(dayOfMonth: Int, amount: Double, adjustForWeekends: Boolean)
    fun get(): Flow<IncomeSchedule?>
    suspend fun clear()
}
