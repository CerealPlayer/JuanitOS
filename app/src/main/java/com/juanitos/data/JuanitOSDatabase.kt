package com.juanitos.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Setting::class, Food::class, FoodIngredient::class, Ingredient::class], version = 5, exportSchema = false)
abstract class JuanitOSDatabase : RoomDatabase() {
    abstract fun settingDao(): SettingDao
    abstract fun foodDao(): FoodDao
    abstract fun foodIngredientDao(): FoodIngredientDao
    abstract fun ingredientDao(): IngredientDao

    companion object {
        @Volatile
        private var Instance: JuanitOSDatabase? = null

        fun getDatabase(context: Context) : JuanitOSDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, JuanitOSDatabase::class.java, "JuanitOS_database")
                    .fallbackToDestructiveMigration(false)
                    .build().also { Instance = it }
            }
        }
    }
}
