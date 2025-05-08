package com.juanitos.data.food.offline

import com.juanitos.data.food.daos.FoodDao
import com.juanitos.data.food.entities.Food
import com.juanitos.data.food.entities.relations.FoodDetails
import com.juanitos.data.food.repositories.FoodRepository
import kotlinx.coroutines.flow.Flow

class OfflineFoodRepository(private val foodDao: FoodDao) : FoodRepository {
    override fun getAllFoodsStream(): Flow<List<Food>> = foodDao.getAllFoods()
    override fun getFoodStream(id: Int): Flow<Food?> = foodDao.getFood(id)
    override fun getTodaysFoodsStream(): Flow<List<FoodDetails>> = foodDao.getTodaysFoods()
    override fun getFoodDetailsStream(id: Int): Flow<FoodDetails?> = foodDao.getFoodDetails(id)
    override suspend fun insertFood(name: String) = foodDao.insert(name)
    override suspend fun deleteFood(food: Food) = foodDao.delete(food)
    override suspend fun updateFood(food: Food) = foodDao.update(food)
}
