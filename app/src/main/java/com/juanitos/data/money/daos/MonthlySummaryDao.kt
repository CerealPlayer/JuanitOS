package com.juanitos.data.money.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.juanitos.data.money.entities.MonthlySummary
import kotlinx.coroutines.flow.Flow

@Dao
interface MonthlySummaryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(summary: MonthlySummary)

    @Query("SELECT * FROM monthly_summaries WHERE account_id = :accountId ORDER BY month")
    fun getByAccount(accountId: Int): Flow<List<MonthlySummary>>

    @Query("SELECT * FROM monthly_summaries WHERE account_id = :accountId AND month = :month LIMIT 1")
    suspend fun getByAccountAndMonth(accountId: Int, month: String): MonthlySummary?

    // Self-creating upsert: a balance-affecting transaction landing in a month with no existing
    // row (the live current month, or a backdated entry into an unfinalized past month) seeds one.
    @Query(
        "INSERT INTO monthly_summaries (account_id, month, total_income, total_expenses, net_balance) " +
                "VALUES (:accountId, :month, :incomeDelta, :expenseDelta, :incomeDelta - :expenseDelta) " +
                "ON CONFLICT(account_id, month) DO UPDATE SET " +
                "total_income = total_income + :incomeDelta, " +
                "total_expenses = total_expenses + :expenseDelta, " +
                "net_balance = net_balance + :incomeDelta - :expenseDelta"
    )
    suspend fun adjust(accountId: Int, month: String, incomeDelta: Double, expenseDelta: Double)
}
