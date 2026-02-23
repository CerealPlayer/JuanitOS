package com.juanitos.data.workout.repositories

import com.juanitos.data.workout.entities.ExerciseDefinition
import kotlinx.coroutines.flow.Flow

interface ExerciseDefinitionRepository {
    suspend fun insert(exerciseDefinition: ExerciseDefinition): Long
    suspend fun update(exerciseDefinition: ExerciseDefinition)
    suspend fun delete(exerciseDefinition: ExerciseDefinition)
    fun getById(id: Int): Flow<ExerciseDefinition>
    fun getAll(): Flow<List<ExerciseDefinition>>
    fun getAllByType(type: String): Flow<List<ExerciseDefinition>>
}
