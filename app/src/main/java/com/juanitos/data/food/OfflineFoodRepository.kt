package com.juanitos.data.food

import kotlinx.coroutines.flow.Flow

class OfflineFoodRepository(private val foodDao: FoodDao) : FoodRepository {
    override fun getAllFoodsStream(): Flow<List<Food>> = foodDao.getAllFoods()
    override fun getFoodStream(id: Int): Flow<Food?> = foodDao.getFood(id)
    override fun getTodaysFoodsStream(): Flow<List<FoodIngredientDetails>> = foodDao.getTodaysFoods()
    override suspend fun insertFood(name: String) = foodDao.insert(name)
    override suspend fun deleteFood(food: Food) = foodDao.delete(food)
    override suspend fun updateFood(food: Food) = foodDao.update(food)
}
