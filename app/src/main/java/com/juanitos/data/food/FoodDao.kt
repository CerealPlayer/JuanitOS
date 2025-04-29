package com.juanitos.data.food

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDao {
    @Query("insert into foods (name) values (:name)")
    suspend fun insert(name: String): Long

    @Update
    suspend fun update(food: Food)

    @Delete
    suspend fun delete(food: Food)

    @Query("select * from foods where id = :id")
    fun getFood(id: Int): Flow<Food>

    @Query("select * from foods order by id asc")
    fun getAllFoods(): Flow<List<Food>>

    @Query("""
    SELECT 
        foods.id AS food_id,
        foods.name AS food_name,
        foods.created_at,
        ingredients.id AS ingredient_id,
        ingredients.name AS ingredient_name,
        ingredients.calories_per_100 as ingredient_calories_per_100,
        ingredients.proteins_per_100 as ingredient_proteins_per_100,
        food_ingredients.grams 
    FROM foods 
    INNER JOIN food_ingredients ON foods.id = food_ingredients.food_id 
    INNER JOIN ingredients ON food_ingredients.ingredient_id = ingredients.id 
    WHERE date(foods.created_at) = date('now')
""")
    fun getTodaysFoods(): Flow<List<FoodIngredientDetails>>
}
