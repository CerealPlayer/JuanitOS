package com.juanitos.data.workout.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.juanitos.data.workout.entities.ExerciseDefinition
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDefinitionDao {
    @Insert
    suspend fun insert(exerciseDefinition: ExerciseDefinition): Long

    @Update
    suspend fun update(exerciseDefinition: ExerciseDefinition)

    @Delete
    suspend fun delete(exerciseDefinition: ExerciseDefinition)

    @Query("SELECT * FROM exercise_definitions WHERE id = :id")
    fun getById(id: Int): Flow<ExerciseDefinition>

    @Query("SELECT * FROM exercise_definitions ORDER BY name ASC")
    fun getAll(): Flow<List<ExerciseDefinition>>

    @Query("SELECT * FROM exercise_definitions WHERE type = :type ORDER BY name ASC")
    fun getAllByType(type: String): Flow<List<ExerciseDefinition>>
}
