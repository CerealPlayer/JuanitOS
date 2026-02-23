package com.juanitos.data.workout.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.juanitos.data.workout.entities.Workout
import com.juanitos.data.workout.entities.relations.WorkoutWithExercises
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Insert
    suspend fun insert(workout: Workout): Long

    @Update
    suspend fun update(workout: Workout)

    @Delete
    suspend fun delete(workout: Workout)

    @Query("SELECT * FROM workouts WHERE id = :id")
    fun getById(id: Int): Flow<Workout>

    @Transaction
    @Query("SELECT * FROM workouts WHERE id = :id")
    fun getByIdWithExercises(id: Int): Flow<WorkoutWithExercises>

    @Query("SELECT * FROM workouts ORDER BY date DESC")
    fun getAll(): Flow<List<Workout>>

    @Transaction
    @Query("SELECT * FROM workouts ORDER BY date DESC")
    fun getAllWithExercises(): Flow<List<WorkoutWithExercises>>
}
