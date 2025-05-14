package com.juanitos.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.juanitos.data.food.daos.BatchFoodDao
import com.juanitos.data.food.daos.BatchFoodIngredientDao
import com.juanitos.data.food.daos.FoodDao
import com.juanitos.data.food.daos.FoodIngredientDao
import com.juanitos.data.food.daos.IngredientDao
import com.juanitos.data.food.daos.SettingDao
import com.juanitos.data.food.entities.BatchFood
import com.juanitos.data.food.entities.BatchFoodIngredient
import com.juanitos.data.food.entities.Food
import com.juanitos.data.food.entities.FoodIngredient
import com.juanitos.data.food.entities.Ingredient
import com.juanitos.data.food.entities.Setting
import com.juanitos.data.migrations.MIGRATION_10_11
import com.juanitos.data.migrations.MIGRATION_11_12
import com.juanitos.data.migrations.MIGRATION_12_13
import com.juanitos.data.migrations.MIGRATION_13_14
import com.juanitos.data.migrations.MIGRATION_9_10

@Database(
    entities = [Setting::class, Food::class, FoodIngredient::class, Ingredient::class, BatchFood::class, BatchFoodIngredient::class],
    version = 14,
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
                    .addMigrations(
                        MIGRATION_9_10,
                        MIGRATION_10_11,
                        MIGRATION_11_12,
                        MIGRATION_12_13,
                        MIGRATION_13_14
                    )
                    .fallbackToDestructiveMigration(false)
                    .build().also { Instance = it }
            }
        }
    }
}
