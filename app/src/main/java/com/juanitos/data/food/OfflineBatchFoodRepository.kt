package com.juanitos.data.food

import kotlinx.coroutines.flow.Flow

class OfflineBatchFoodRepository(private val batchFoodDao: BatchFoodDao): BatchFoodRepository {
    override suspend fun insert(name: String, totalGrams: Int): Long =
        batchFoodDao.insert(name, totalGrams)
    override suspend fun update(batchFood: BatchFood) =
        batchFoodDao.update(batchFood)
    override suspend fun delete(batchFood: BatchFood) =
        batchFoodDao.delete(batchFood)
    override fun getBatchFood(id: Int): Flow<BatchFood> =
        batchFoodDao.getBatchFood(id)
    override fun getAllBatchFoods(): Flow<List<BatchFood>> =
        batchFoodDao.getAllBatchFoods()
}