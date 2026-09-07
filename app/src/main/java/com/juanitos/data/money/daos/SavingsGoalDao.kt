package com.juanitos.data.money.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.juanitos.data.money.entities.SavingsGoal
import kotlinx.coroutines.flow.Flow

@Dao
interface SavingsGoalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(goal: SavingsGoal)

    @Query("SELECT * FROM savings_goals WHERE account_id = :accountId LIMIT 1")
    fun getByAccount(accountId: Int): Flow<SavingsGoal?>

    @Query("SELECT * FROM savings_goals WHERE account_id = :accountId LIMIT 1")
    suspend fun getByAccountOnce(accountId: Int): SavingsGoal?

    @Query("SELECT * FROM savings_goals WHERE notifications_enabled = 1")
    suspend fun getAllWithNotificationsEnabled(): List<SavingsGoal>

    @Query("UPDATE savings_goals SET last_risk_notified_month = :month WHERE id = :goalId")
    suspend fun markRiskNotified(goalId: Int, month: String)
}
