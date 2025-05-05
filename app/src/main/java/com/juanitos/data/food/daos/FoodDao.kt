package com.juanitos.data.food.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Update
import com.juanitos.data.food.entities.Food
import com.juanitos.data.food.entities.relations.FoodIngredientDetails
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
    select 
        foods.id as food_id,
        foods.name as food_name,
        foods.created_at,
        ingredients.id as ingredient_id,
        ingredients.name as ingredient_name,
        ingredients.calories_per_100 as ingredient_calories_per_100,
        ingredients.proteins_per_100 as ingredient_proteins_per_100,
        food_ingredients.grams 
    from foods 
    inner join food_ingredients on foods.id = food_ingredients.food_id 
    inner join ingredients on food_ingredients.ingredient_id = ingredients.id 
    where date(foods.created_at) = date('now')
""")
    fun getTodaysFoods(): Flow<List<FoodIngredientDetails>>
}
