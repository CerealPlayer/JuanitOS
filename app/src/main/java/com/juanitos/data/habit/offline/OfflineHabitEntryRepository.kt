package com.juanitos.data.habit.offline

import com.juanitos.data.habit.daos.HabitEntryDao
import com.juanitos.data.habit.entities.HabitEntry
import com.juanitos.data.habit.repositories.HabitEntryRepository
import kotlinx.coroutines.flow.Flow

class OfflineHabitEntryRepository(private val habitEntryDao: HabitEntryDao) : HabitEntryRepository {
    override suspend fun insert(entry: HabitEntry): Long = habitEntryDao.insert(entry)
    override suspend fun update(entry: HabitEntry) = habitEntryDao.update(entry)
    override suspend fun delete(entry: HabitEntry) = habitEntryDao.delete(entry)
    override suspend fun getByHabitIdAndDate(habitId: Int, date: String): HabitEntry? =
        habitEntryDao.getByHabitIdAndDate(habitId, date)

    override fun getByDate(date: String): Flow<List<HabitEntry>> = habitEntryDao.getByDate(date)
}
