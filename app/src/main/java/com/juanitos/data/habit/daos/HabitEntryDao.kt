package com.juanitos.data.habit.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.juanitos.data.habit.entities.HabitEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitEntryDao {
    @Insert
    suspend fun insert(entry: HabitEntry): Long

    @Update
    suspend fun update(entry: HabitEntry)

    @Delete
    suspend fun delete(entry: HabitEntry)

    @Query("SELECT * FROM habit_entries WHERE habit_id = :habitId AND date = :date LIMIT 1")
    suspend fun getByHabitIdAndDate(habitId: Int, date: String): HabitEntry?

    @Query("SELECT * FROM habit_entries WHERE date = :date")
    fun getByDate(date: String): Flow<List<HabitEntry>>
}
