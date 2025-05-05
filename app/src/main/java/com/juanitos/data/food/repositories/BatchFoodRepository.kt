package com.juanitos.data.food.repositories

import com.juanitos.data.food.entities.BatchFood
import com.juanitos.data.food.entities.relations.BatchFoodWithIngredientDetails
import kotlinx.coroutines.flow.Flow

interface BatchFoodRepository {
    suspend fun insert(name: String, totalGrams: Int): Long
    suspend fun update(batchFood: BatchFood)
    suspend fun delete(batchFood: BatchFood)
    fun getBatchFood(id: Int): Flow<BatchFood>
    fun getAllBatchFoods(): Flow<List<BatchFood>>
    fun getBatchFoodsWithIngredients(): Flow<List<BatchFoodWithIngredientDetails>>
}