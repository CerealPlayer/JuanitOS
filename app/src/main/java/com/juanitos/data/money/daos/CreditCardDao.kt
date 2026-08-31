package com.juanitos.data.money.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Update
import com.juanitos.data.money.entities.CreditCard
import kotlinx.coroutines.flow.Flow

@Dao
interface CreditCardDao {
    @Query("INSERT INTO credit_cards (account_id, name, payment_day) VALUES (:accountId, :name, :paymentDay)")
    suspend fun insert(accountId: Int, name: String, paymentDay: Int): Long

    @Update
    suspend fun update(creditCard: CreditCard)

    @Delete
    suspend fun delete(creditCard: CreditCard)

    @Query("SELECT * FROM credit_cards WHERE id = :id")
    fun getById(id: Int): Flow<CreditCard>

    @Query("SELECT * FROM credit_cards ORDER BY created_at")
    fun getAll(): Flow<List<CreditCard>>

    @Query("SELECT * FROM credit_cards WHERE account_id = :accountId ORDER BY created_at")
    fun getByAccountId(accountId: Int): Flow<List<CreditCard>>

    @Query("SELECT * FROM credit_cards WHERE account_id = :accountId")
    suspend fun getByAccountIdOnce(accountId: Int): List<CreditCard>

    @Query("UPDATE credit_cards SET last_settled_at = :date WHERE id = :id")
    suspend fun updateLastSettledAt(id: Int, date: String)
}
