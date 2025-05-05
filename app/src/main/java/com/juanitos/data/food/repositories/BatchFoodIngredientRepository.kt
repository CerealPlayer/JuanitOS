package com.juanitos.data.food.repositories

import com.juanitos.data.food.entities.BatchFoodIngredient
import kotlinx.coroutines.flow.Flow

interface BatchFoodIngredientRepository {
    suspend fun insert(batchFoodIngredient: BatchFoodIngredient): Long
    suspend fun update(batchFoodIngredient: BatchFoodIngredient)
    suspend fun delete(batchFoodIngredient: BatchFoodIngredient)
    fun getBatchFoodIngredient(id: Int): Flow<BatchFoodIngredient>
    fun getAllBatchFoodIngredients(): Flow<List<BatchFoodIngredient>>
}