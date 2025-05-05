package com.juanitos.data.food.offline

import com.juanitos.data.food.daos.FoodIngredientDao
import com.juanitos.data.food.entities.FoodIngredient
import com.juanitos.data.food.repositories.FoodIngredientRepository
import kotlinx.coroutines.flow.Flow

class OfflineFoodIngredientRepository(private val foodIngredientDao: FoodIngredientDao) :
    FoodIngredientRepository {
    override fun getAllFoodIngredientsStream(): Flow<List<FoodIngredient>> = foodIngredientDao.getAllFoodIngredients()
    override fun getFoodIngredientStream(id: Int): Flow<FoodIngredient?> = foodIngredientDao.getFoodIngredient(id)
    override suspend fun insertFoodIngredient(foodIngredient: FoodIngredient) = foodIngredientDao.insert(foodIngredient)
    override suspend fun deleteFoodIngredient(foodIngredient: FoodIngredient) = foodIngredientDao.delete(foodIngredient)
    override suspend fun updateFoodIngredient(foodIngredient: FoodIngredient) = foodIngredientDao.update(foodIngredient)
}
