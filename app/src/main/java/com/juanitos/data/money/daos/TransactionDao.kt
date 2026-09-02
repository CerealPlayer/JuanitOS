package com.juanitos.data.money.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Update
import com.juanitos.data.money.entities.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query(
        "INSERT INTO transactions (account_id, amount, category_id, description, created_at, frequency, recurrence_root_id, credit_card_id) " +
                "VALUES (:accountId, :amount, :category, :description, :createdAt, :frequency, :recurrenceRootId, :creditCardId)"
    )
    suspend fun insert(
        accountId: Int,
        amount: Double,
        category: Int,
        description: String?,
        createdAt: String,
        frequency: String?,
        recurrenceRootId: Int?,
        creditCardId: Int?
    ): Long

    @Update
    suspend fun update(transaction: Transaction)

    @Delete
    suspend fun delete(transaction: Transaction)

    @Query("SELECT * FROM transactions WHERE id = :id")
    fun getById(id: Int): Flow<Transaction>

    @Query("SELECT * FROM transactions WHERE account_id = :accountId AND frequency IS NOT NULL AND recurrence_root_id IS NULL")
    suspend fun getRecurringTemplates(accountId: Int): List<Transaction>

    @Query("SELECT MAX(created_at) FROM transactions WHERE id = :templateId OR recurrence_root_id = :templateId")
    suspend fun getLatestOccurrenceDate(templateId: Int): String?

    @Query("DELETE FROM transactions WHERE recurrence_root_id = :templateId")
    suspend fun deleteByRecurrenceRoot(templateId: Int)

    @Query(
        "SELECT COALESCE(SUM(amount), 0) FROM transactions " +
                "WHERE credit_card_id = :cardId AND created_at > :after AND created_at <= :upTo"
    )
    suspend fun sumCreditCardTransactions(cardId: Int, after: String, upTo: String): Double

    @Query(
        "SELECT substr(created_at, 1, 7) AS month, " +
                "COALESCE(SUM(CASE WHEN amount < 0 THEN -amount ELSE 0 END), 0) AS income, " +
                "COALESCE(SUM(CASE WHEN amount > 0 THEN amount ELSE 0 END), 0) AS expenses " +
                "FROM transactions WHERE recurrence_root_id = :templateId AND credit_card_id IS NULL " +
                "AND created_at <= :now GROUP BY substr(created_at, 1, 7)"
    )
    suspend fun sumBalanceAffectingByRecurrenceRootGroupedByMonth(
        templateId: Int,
        now: String
    ): List<MonthlyIncomeExpenseTotals>

    @Query(
        "SELECT substr(created_at, 1, 7) AS month, " +
                "COALESCE(SUM(CASE WHEN amount < 0 THEN -amount ELSE 0 END), 0) AS income, " +
                "COALESCE(SUM(CASE WHEN amount > 0 THEN amount ELSE 0 END), 0) AS expenses " +
                "FROM transactions WHERE account_id = :accountId AND credit_card_id IS NULL " +
                "AND created_at <= :now AND created_at > :lastSweptAt " +
                "GROUP BY substr(created_at, 1, 7)"
    )
    suspend fun sumNewlyDueTransactionsGroupedByMonth(
        accountId: Int,
        now: String,
        lastSweptAt: String
    ): List<MonthlyIncomeExpenseTotals>

    @Query(
        "SELECT COALESCE(SUM(CASE WHEN amount < 0 THEN -amount ELSE 0 END), 0) AS income, " +
                "COALESCE(SUM(CASE WHEN amount > 0 THEN amount ELSE 0 END), 0) AS expenses " +
                "FROM transactions WHERE account_id = :accountId AND credit_card_id IS NULL " +
                "AND created_at LIKE :monthPrefix || '%'"
    )
    suspend fun sumIncomeAndExpenses(accountId: Int, monthPrefix: String): IncomeExpenseTotals
}

data class IncomeExpenseTotals(val income: Double, val expenses: Double)
data class MonthlyIncomeExpenseTotals(val month: String, val income: Double, val expenses: Double)
