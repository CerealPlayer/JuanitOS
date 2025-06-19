package com.juanitos.data.money.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.juanitos.data.money.entities.FixedSpending
import kotlinx.coroutines.flow.Flow

@Dao
interface FixedSpendingDao {
    @Insert
    suspend fun insert(fixedSpending: FixedSpending)

    @Update
    suspend fun update(fixedSpending: FixedSpending)

    @Delete
    suspend fun delete(fixedSpending: FixedSpending)

    @Query("SELECT * FROM fixed_spendings WHERE id = :id")
    fun getById(id: Int): Flow<FixedSpending>
}
