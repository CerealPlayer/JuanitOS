package com.juanitos.data.food.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.juanitos.data.food.entities.Food
import com.juanitos.data.food.entities.relations.FoodDetails
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

    @Transaction
    @Query("""
        select * from foods 
        where date(created_at) = date('now', 'localtime') 
        order by id desc
    """
    )
    fun getTodaysFoods(): Flow<List<FoodDetails>>

    @Transaction
    @Query("select * from foods where id = :id")
    fun getFoodDetails(id: Int): Flow<FoodDetails?>
}
