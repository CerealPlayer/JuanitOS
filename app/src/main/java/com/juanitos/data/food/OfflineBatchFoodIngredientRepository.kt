package com.juanitos.data.food

import kotlinx.coroutines.flow.Flow

class OfflineBatchFoodIngredientRepository(private val batchFoodIngredientDao: BatchFoodIngredientDao) :
    BatchFoodIngredientRepository {
    override suspend fun insert(batchFoodIngredient: BatchFoodIngredient): Long =
        batchFoodIngredientDao.insert(batchFoodIngredient)

    override suspend fun update(batchFoodIngredient: BatchFoodIngredient) =
        batchFoodIngredientDao.update(batchFoodIngredient)

    override suspend fun delete(batchFoodIngredient: BatchFoodIngredient) =
        batchFoodIngredientDao.delete(batchFoodIngredient)

    override fun getBatchFoodIngredient(id: Int): Flow<BatchFoodIngredient> =
        batchFoodIngredientDao.getBatchFoodIngredient(id)

    override fun getAllBatchFoodIngredients(): Flow<List<BatchFoodIngredient>> =
        batchFoodIngredientDao.getAllBatchFoodIngredients()
}