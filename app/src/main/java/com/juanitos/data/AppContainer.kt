package com.juanitos.data

import android.content.Context

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
