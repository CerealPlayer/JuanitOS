package com.juanitos.data.food

import kotlinx.coroutines.flow.Flow

interface FoodRepository {
    fun getAllFoodsStream(): Flow<List<Food>>
    fun getFoodStream(id: Int): Flow<Food?>
    fun getTodaysFoodsStream(): Flow<List<Food>>
    suspend fun insertFood(name: String): Long
    suspend fun deleteFood(food: Food)
    suspend fun updateFood(food: Food)
}
