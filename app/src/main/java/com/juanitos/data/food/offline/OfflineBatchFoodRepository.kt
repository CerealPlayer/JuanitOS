package com.juanitos.data.food.offline

import com.juanitos.data.food.daos.BatchFoodDao
import com.juanitos.data.food.entities.BatchFood
import com.juanitos.data.food.entities.relations.BatchFoodWithIngredientDetails
import com.juanitos.data.food.repositories.BatchFoodRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class OfflineBatchFoodRepository(private val batchFoodDao: BatchFoodDao) : BatchFoodRepository {
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

    override fun getBatchFoodsWithIngredients(): Flow<List<BatchFoodWithIngredientDetails>> =
        batchFoodDao.getBatchFoodsWithIngredients()
            .map { it -> it.map { it.toBatchFoodWithIngredientDetails() } }

    override fun searchBatchFoods(query: String): Flow<List<BatchFoodWithIngredientDetails>> =
        batchFoodDao.searchBatchFoods(query)
            .map { it -> it.map { it.toBatchFoodWithIngredientDetails() } }
}