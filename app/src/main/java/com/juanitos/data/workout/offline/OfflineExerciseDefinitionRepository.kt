package com.juanitos.data.workout.offline

import com.juanitos.data.workout.daos.ExerciseDefinitionDao
import com.juanitos.data.workout.entities.ExerciseDefinition
import com.juanitos.data.workout.repositories.ExerciseDefinitionRepository
import kotlinx.coroutines.flow.Flow

data class OfflineExerciseDefinitionRepository(
    private val exerciseDefinitionDao: ExerciseDefinitionDao
) : ExerciseDefinitionRepository {
    override suspend fun insert(exerciseDefinition: ExerciseDefinition): Long =
        exerciseDefinitionDao.insert(exerciseDefinition)

    override suspend fun update(exerciseDefinition: ExerciseDefinition) =
        exerciseDefinitionDao.update(exerciseDefinition)

    override suspend fun delete(exerciseDefinition: ExerciseDefinition) =
        exerciseDefinitionDao.delete(exerciseDefinition)

    override fun getById(id: Int): Flow<ExerciseDefinition> = exerciseDefinitionDao.getById(id)
    override fun getAll(): Flow<List<ExerciseDefinition>> = exerciseDefinitionDao.getAll()
    override fun getAllByType(type: String): Flow<List<ExerciseDefinition>> =
        exerciseDefinitionDao.getAllByType(type)
}
