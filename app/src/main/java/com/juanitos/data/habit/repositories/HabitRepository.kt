package com.juanitos.data.habit.repositories

import com.juanitos.data.habit.entities.Habit
import com.juanitos.data.habit.entities.relations.HabitWithEntries
import kotlinx.coroutines.flow.Flow

interface HabitRepository {
    suspend fun insert(habit: Habit): Long
    suspend fun update(habit: Habit)
    suspend fun delete(habit: Habit)
    fun getAll(): Flow<List<Habit>>
    fun getAllWithEntries(): Flow<List<HabitWithEntries>>
    fun getNewestWithEntries(limit: Int): Flow<List<HabitWithEntries>>
    fun getNewestActiveWithEntries(limit: Int): Flow<List<HabitWithEntries>>
    fun getByIdWithEntries(habitId: Int): Flow<HabitWithEntries?>
}
