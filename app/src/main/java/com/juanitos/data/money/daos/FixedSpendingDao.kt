package com.juanitos.data.money.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Update
import com.juanitos.data.money.entities.FixedSpending
import kotlinx.coroutines.flow.Flow

@Dao
interface FixedSpendingDao {
    @Query("INSERT INTO fixed_spendings (cycle_id, amount, category, description) VALUES (:cycleId, :amount, :category, :description)")
    suspend fun insert(cycleId: Int, amount: Double, category: String, description: String?): Long

    @Update
    suspend fun update(fixedSpending: FixedSpending)

    @Delete
    suspend fun delete(fixedSpending: FixedSpending)

    @Query("SELECT * FROM fixed_spendings WHERE id = :id")
    fun getById(id: Int): Flow<FixedSpending>
}
