package com.juanitos.data.money.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.juanitos.data.money.entities.FixedSpending

@Dao
interface FixedSpendingDao {
    @Insert
    suspend fun insertFixedSpending(fixedSpending: FixedSpending)

    @Update
    suspend fun updateFixedSpending(fixedSpending: FixedSpending)

    @Delete
    suspend fun deleteFixedSpending(fixedSpending: FixedSpending)

    @Query("SELECT * FROM fixed_spendings WHERE id = :spendingId")
    suspend fun getFixedSpendingsByCycleId(spendingId: Int): List<FixedSpending>
}
