package com.juanitos.data.money.daos

import androidx.room.Dao
import androidx.room.Query
import com.juanitos.data.money.entities.IncomeSchedule
import kotlinx.coroutines.flow.Flow

@Dao
interface IncomeScheduleDao {
    @Query(
        "INSERT OR REPLACE INTO income_schedules (id, day_of_month, amount, adjust_for_weekends) " +
                "VALUES (1, :dayOfMonth, :amount, :adjustForWeekends)"
    )
    suspend fun upsert(dayOfMonth: Int, amount: Double, adjustForWeekends: Boolean)

    @Query("SELECT * FROM income_schedules WHERE id = 1 LIMIT 1")
    fun get(): Flow<IncomeSchedule?>

    @Query("DELETE FROM income_schedules WHERE id = 1")
    suspend fun clear()
}
