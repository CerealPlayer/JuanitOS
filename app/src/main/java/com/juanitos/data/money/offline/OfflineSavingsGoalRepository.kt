package com.juanitos.data.money.offline

import com.juanitos.data.money.daos.SavingsGoalDao
import com.juanitos.data.money.entities.SavingsGoal
import com.juanitos.data.money.repositories.SavingsGoalRepository
import kotlinx.coroutines.flow.Flow

class OfflineSavingsGoalRepository(
    private val savingsGoalDao: SavingsGoalDao,
) : SavingsGoalRepository {
    override fun getByAccount(accountId: Int): Flow<SavingsGoal?> =
        savingsGoalDao.getByAccount(accountId)

    override suspend fun getByAccountOnce(accountId: Int): SavingsGoal? =
        savingsGoalDao.getByAccountOnce(accountId)

    override suspend fun getAllWithNotificationsEnabled(): List<SavingsGoal> =
        savingsGoalDao.getAllWithNotificationsEnabled()

    override suspend fun upsert(goal: SavingsGoal) = savingsGoalDao.upsert(goal)

    override suspend fun markRiskNotified(goalId: Int, month: String) =
        savingsGoalDao.markRiskNotified(goalId, month)
}
