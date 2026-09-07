package com.juanitos.data.money.repositories

import com.juanitos.data.money.entities.SavingsGoal
import kotlinx.coroutines.flow.Flow

interface SavingsGoalRepository {
    fun getByAccount(accountId: Int): Flow<SavingsGoal?>
    suspend fun getByAccountOnce(accountId: Int): SavingsGoal?
    suspend fun getAllWithNotificationsEnabled(): List<SavingsGoal>
    suspend fun upsert(goal: SavingsGoal)
    suspend fun markRiskNotified(goalId: Int, month: String)
}
