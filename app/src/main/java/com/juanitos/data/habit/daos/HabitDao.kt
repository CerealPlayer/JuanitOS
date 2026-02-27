package com.juanitos.data.habit.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.juanitos.data.habit.entities.Habit
import com.juanitos.data.habit.entities.relations.HabitWithEntries
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Query("INSERT INTO habits (name, description) VALUES (:name, :description)")
    suspend fun insert(name: String, description: String?): Long

    @Update
    suspend fun update(habit: Habit)

    @Delete
    suspend fun delete(habit: Habit)

    @Query("SELECT * FROM habits ORDER BY created_at ASC")
    fun getAll(): Flow<List<Habit>>

    @Transaction
    @Query("SELECT * FROM habits ORDER BY created_at ASC")
    fun getAllWithEntries(): Flow<List<HabitWithEntries>>

    @Transaction
    @Query("SELECT * FROM habits ORDER BY datetime(created_at) DESC, id DESC LIMIT :limit")
    fun getNewestWithEntries(limit: Int): Flow<List<HabitWithEntries>>

    @Transaction
    @Query("SELECT * FROM habits WHERE completed_at IS NULL ORDER BY datetime(created_at) DESC, id DESC LIMIT :limit")
    fun getNewestActiveWithEntries(limit: Int): Flow<List<HabitWithEntries>>

    @Transaction
    @Query("SELECT * FROM habits WHERE id = :habitId LIMIT 1")
    fun getByIdWithEntries(habitId: Int): Flow<HabitWithEntries?>
}
