package com.juanitos.data.habit.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.juanitos.data.habit.entities.Habit
import com.juanitos.data.habit.entities.relations.HabitWithEntries
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Insert
    suspend fun insert(habit: Habit): Long

    @Update
    suspend fun update(habit: Habit)

    @Delete
    suspend fun delete(habit: Habit)

    @Query("SELECT * FROM habits ORDER BY created_at ASC")
    fun getAll(): Flow<List<Habit>>

    @Transaction
    @Query("SELECT * FROM habits ORDER BY created_at ASC")
    fun getAllWithEntries(): Flow<List<HabitWithEntries>>
}
