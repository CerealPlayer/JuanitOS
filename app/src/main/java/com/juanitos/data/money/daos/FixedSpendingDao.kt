package com.juanitos.data.money.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Update
import com.juanitos.data.money.entities.FixedSpending
import com.juanitos.data.money.entities.relations.FixedSpendingWithCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface FixedSpendingDao {
    @Query("INSERT INTO fixed_spendings (amount, category_id, description, active) VALUES (:amount, :category, :description, :active)")
    suspend fun insert(
        amount: Double,
        category: Int,
        description: String?,
        active: Boolean = true
    ): Long

    @Update
    suspend fun update(fixedSpending: FixedSpending)

    @Delete
    suspend fun delete(fixedSpending: FixedSpending)

    @Query("SELECT * FROM fixed_spendings WHERE id = :id")
    fun getById(id: Int): Flow<FixedSpendingWithCategory>

    @Query("SELECT * FROM fixed_spendings WHERE deleted_at IS NULL ORDER BY id ASC")
    fun getAll(): Flow<List<FixedSpendingWithCategory>>

    @Query("update fixed_spendings set active = :enabled where id = :spendingId")
    suspend fun updateEnabled(spendingId: Int, enabled: Boolean)
}
