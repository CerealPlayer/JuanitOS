package com.juanitos.data.food.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.juanitos.data.food.entities.FoodIngredient
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodIngredientDao {
    @Insert
    suspend fun insert(foodIngredient: FoodIngredient)

    @Update
    suspend fun update(foodIngredient: FoodIngredient)

    @Delete
    suspend fun delete(foodIngredient: FoodIngredient)

    @Query("select * from food_ingredients where id = :id")
    fun getFoodIngredient(id: Int): Flow<FoodIngredient>

    @Query("select * from food_ingredients order by id asc")
    fun getAllFoodIngredients(): Flow<List<FoodIngredient>>
}
