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
}
