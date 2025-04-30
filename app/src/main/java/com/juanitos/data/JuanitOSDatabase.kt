package com.juanitos.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.juanitos.data.food.BatchFood
import com.juanitos.data.food.BatchFoodDao
import com.juanitos.data.food.BatchFoodIngredient
import com.juanitos.data.food.BatchFoodIngredientDao
import com.juanitos.data.food.Food
import com.juanitos.data.food.FoodDao
import com.juanitos.data.food.FoodIngredient
import com.juanitos.data.food.FoodIngredientDao
import com.juanitos.data.food.Ingredient
import com.juanitos.data.food.IngredientDao
import com.juanitos.data.food.Setting
import com.juanitos.data.food.SettingDao

@Database(
    entities = [Setting::class, Food::class, FoodIngredient::class, Ingredient::class, BatchFood::class, BatchFoodIngredient::class],
    version = 6,
    exportSchema = false
)
abstract class JuanitOSDatabase : RoomDatabase() {
    abstract fun settingDao(): SettingDao
    abstract fun foodDao(): FoodDao
    abstract fun foodIngredientDao(): FoodIngredientDao
    abstract fun ingredientDao(): IngredientDao
    abstract fun batchFoodDao(): BatchFoodDao
    abstract fun batchFoodIngredientDao(): BatchFoodIngredientDao

    companion object {
        @Volatile
        private var Instance: JuanitOSDatabase? = null

        fun getDatabase(context: Context): JuanitOSDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, JuanitOSDatabase::class.java, "JuanitOS_database")
                    .fallbackToDestructiveMigration(false)
                    .build().also { Instance = it }
            }
        }
    }
}
