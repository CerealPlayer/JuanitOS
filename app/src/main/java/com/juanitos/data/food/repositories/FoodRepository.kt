package com.juanitos.data.food.repositories

import com.juanitos.data.food.entities.Food
import com.juanitos.data.food.entities.relations.FoodIngredientDetails
import kotlinx.coroutines.flow.Flow

interface FoodRepository {
    fun getAllFoodsStream(): Flow<List<Food>>
    fun getFoodStream(id: Int): Flow<Food?>
    fun getTodaysFoodsStream(): Flow<List<FoodIngredientDetails>>
    suspend fun insertFood(name: String): Long
    suspend fun deleteFood(food: Food)
    suspend fun updateFood(food: Food)
}
