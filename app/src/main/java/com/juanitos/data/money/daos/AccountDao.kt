package com.juanitos.data.money.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.juanitos.data.money.entities.Account
import com.juanitos.data.money.entities.relations.AccountWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Query("INSERT INTO accounts (name, starting_balance) VALUES (:name, :startingBalance)")
    suspend fun insert(name: String, startingBalance: Double): Long

    @Update
    suspend fun update(account: Account)

    @Delete
    suspend fun delete(account: Account)

    @Query("SELECT * FROM accounts ORDER BY created_at")
    fun getAll(): Flow<List<Account>>

    @Transaction
    @Query("SELECT * FROM accounts WHERE is_selected = 1 LIMIT 1")
    fun getSelected(): Flow<AccountWithDetails?>

    @Query("UPDATE accounts SET is_selected = 0")
    suspend fun clearSelection()

    @Query("UPDATE accounts SET is_selected = 1 WHERE id = :id")
    suspend fun setSelected(id: Int)

    @Transaction
    suspend fun selectAccount(id: Int) {
        clearSelection()
        setSelected(id)
    }
}
