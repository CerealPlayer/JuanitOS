package com.juanitos.data.money.daos

import androidx.room.Dao
import androidx.room.Query
import com.juanitos.data.money.entities.IncomeSchedule
import kotlinx.coroutines.flow.Flow

@Dao
interface IncomeScheduleDao {
    @Query("INSERT OR REPLACE INTO income_schedules (id, day_of_month, amount) VALUES (1, :dayOfMonth, :amount)")
    suspend fun upsert(dayOfMonth: Int, amount: Double)

    @Query("SELECT * FROM income_schedules WHERE id = 1 LIMIT 1")
    fun get(): Flow<IncomeSchedule?>

    @Query("DELETE FROM income_schedules WHERE id = 1")
    suspend fun clear()
}
