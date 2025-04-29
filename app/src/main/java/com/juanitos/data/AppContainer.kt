package com.juanitos.data

import android.content.Context
import com.juanitos.data.food.FoodIngredientRepository
import com.juanitos.data.food.FoodRepository
import com.juanitos.data.food.IngredientRepository
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
}
