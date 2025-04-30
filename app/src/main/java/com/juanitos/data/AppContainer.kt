package com.juanitos.data

import android.content.Context
import com.juanitos.data.food.BatchFoodIngredientRepository
import com.juanitos.data.food.BatchFoodRepository
import com.juanitos.data.food.FoodIngredientRepository
import com.juanitos.data.food.FoodRepository
import com.juanitos.data.food.IngredientRepository
import com.juanitos.data.food.OfflineBatchFoodIngredientRepository
import com.juanitos.data.food.OfflineBatchFoodRepository
import com.juanitos.data.food.OfflineFoodIngredientRepository
import com.juanitos.data.food.OfflineFoodRepository
import com.juanitos.data.food.OfflineIngredientRepository
import com.juanitos.data.food.OfflineSettingsRepository
import com.juanitos.data.food.SettingsRepository

interface AppContainer {
    val settingsRepository: SettingsRepository
    val foodRepository: FoodRepository
    val foodIngredientRepository: FoodIngredientRepository
    val ingredientRepository: IngredientRepository
    val batchFoodRepository: BatchFoodRepository
    val batchFoodIngredientRepository: BatchFoodIngredientRepository
}

class AppDataContainer(private val context: Context) : AppContainer {
    override val settingsRepository: SettingsRepository by lazy {
        OfflineSettingsRepository(settingDao = JuanitOSDatabase.getDatabase(context).settingDao())
    }
    override val foodRepository: FoodRepository by lazy {
        OfflineFoodRepository(foodDao = JuanitOSDatabase.getDatabase(context).foodDao())
    }
    override val foodIngredientRepository: FoodIngredientRepository by lazy {
        OfflineFoodIngredientRepository(foodIngredientDao = JuanitOSDatabase.getDatabase(context).foodIngredientDao())
    }
    override val ingredientRepository: IngredientRepository by lazy {
        OfflineIngredientRepository(ingredientDao = JuanitOSDatabase.getDatabase(context).ingredientDao())
    }
    override val batchFoodRepository: BatchFoodRepository by lazy {
        OfflineBatchFoodRepository(batchFoodDao = JuanitOSDatabase.getDatabase(context).batchFoodDao())
    }
    override val batchFoodIngredientRepository: BatchFoodIngredientRepository by lazy {
        OfflineBatchFoodIngredientRepository(batchFoodIngredientDao = JuanitOSDatabase.getDatabase(context).batchFoodIngredientDao())
    }
}
