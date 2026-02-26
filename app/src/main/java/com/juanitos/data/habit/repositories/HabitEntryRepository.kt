package com.juanitos.data.habit.repositories

import com.juanitos.data.habit.entities.HabitEntry
import kotlinx.coroutines.flow.Flow

interface HabitEntryRepository {
    suspend fun insert(entry: HabitEntry): Long
    suspend fun update(entry: HabitEntry)
    suspend fun delete(entry: HabitEntry)
    suspend fun getByHabitIdAndDate(habitId: Int, date: String): HabitEntry?
    fun getByDate(date: String): Flow<List<HabitEntry>>
}
