package com.juanitos.data.habit.offline

import com.juanitos.data.habit.daos.HabitDao
import com.juanitos.data.habit.entities.Habit
import com.juanitos.data.habit.entities.relations.HabitWithEntries
import com.juanitos.data.habit.repositories.HabitRepository
import kotlinx.coroutines.flow.Flow

class OfflineHabitRepository(private val habitDao: HabitDao) : HabitRepository {
    override suspend fun insert(habit: Habit): Long = habitDao.insert(habit)
    override suspend fun update(habit: Habit) = habitDao.update(habit)
    override suspend fun delete(habit: Habit) = habitDao.delete(habit)
    override fun getAll(): Flow<List<Habit>> = habitDao.getAll()
    override fun getAllWithEntries(): Flow<List<HabitWithEntries>> = habitDao.getAllWithEntries()
}
