package com.juanitos.data.money.offline

import com.juanitos.data.money.daos.IncomeScheduleDao
import com.juanitos.data.money.entities.IncomeSchedule
import com.juanitos.data.money.repositories.IncomeScheduleRepository
import kotlinx.coroutines.flow.Flow

class OfflineIncomeScheduleRepository(private val incomeScheduleDao: IncomeScheduleDao) :
    IncomeScheduleRepository {
    override suspend fun upsert(dayOfMonth: Int, amount: Double, adjustForWeekends: Boolean) =
        incomeScheduleDao.upsert(dayOfMonth, amount, adjustForWeekends)

    override fun get(): Flow<IncomeSchedule?> = incomeScheduleDao.get()
    override suspend fun clear() = incomeScheduleDao.clear()
}
