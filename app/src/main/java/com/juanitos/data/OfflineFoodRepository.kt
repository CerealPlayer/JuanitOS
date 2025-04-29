package com.juanitos.data

import kotlinx.coroutines.flow.Flow

class OfflineFoodRepository(private val foodDao: FoodDao) : FoodRepository {
    override fun getAllFoodsStream(): Flow<List<Food>> = foodDao.getAllFoods()
    override fun getFoodStream(id: Int): Flow<Food?> = foodDao.getFood(id)
    override suspend fun insertFood(name: String) = foodDao.insert(name)
    override suspend fun deleteFood(food: Food) = foodDao.delete(food)
    override suspend fun updateFood(food: Food) = foodDao.update(food)
}
