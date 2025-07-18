package com.juanitos.data.money.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Update
import com.juanitos.data.money.entities.Cycle
import com.juanitos.data.money.entities.relations.CurrentCycleWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface CycleDao {
    @Query("insert into cycles (total_income) values (:income)")
    suspend fun insert(income: Double): Long

    @Update
    suspend fun update(cycle: Cycle)

    @Delete
    suspend fun delete(cycle: Cycle)

    @Query("SELECT * FROM cycles WHERE end_date IS NULL LIMIT 1")
    fun getCurrentCycle(): Flow<CurrentCycleWithDetails?>

    @Query("UPDATE cycles SET end_date = datetime('now', 'localtime') WHERE id = :cycleId")
    suspend fun endCycle(cycleId: Int)
}
