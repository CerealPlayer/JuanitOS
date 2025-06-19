package com.juanitos.data

import android.content.Context
import com.juanitos.data.food.offline.OfflineBatchFoodIngredientRepository
import com.juanitos.data.food.offline.OfflineBatchFoodRepository
import com.juanitos.data.food.offline.OfflineFoodIngredientRepository
import com.juanitos.data.food.offline.OfflineFoodRepository
import com.juanitos.data.food.offline.OfflineIngredientRepository
import com.juanitos.data.food.offline.OfflineSettingsRepository
import com.juanitos.data.food.repositories.BatchFoodIngredientRepository
import com.juanitos.data.food.repositories.BatchFoodRepository
import com.juanitos.data.food.repositories.FoodIngredientRepository
import com.juanitos.data.food.repositories.FoodRepository
import com.juanitos.data.food.repositories.IngredientRepository
import com.juanitos.data.food.repositories.SettingsRepository
import com.juanitos.data.money.offline.OfflineCycleRepository
import com.juanitos.data.money.offline.OfflineFixedSpendingRepository
import com.juanitos.data.money.offline.OfflineTransactionRepository
import com.juanitos.data.money.repositories.CycleRepository
import com.juanitos.data.money.repositories.FixedSpendingRepository
import com.juanitos.data.money.repositories.TransactionRepository

interface AppContainer {
    val settingsRepository: SettingsRepository
    val foodRepository: FoodRepository
    val foodIngredientRepository: FoodIngredientRepository
    val ingredientRepository: IngredientRepository
    val batchFoodRepository: BatchFoodRepository
    val batchFoodIngredientRepository: BatchFoodIngredientRepository
    val cycleRepository: CycleRepository
    val transactionRepository: TransactionRepository
    val fixedSpendingRepository: FixedSpendingRepository
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
    override val cycleRepository: CycleRepository by lazy {
        OfflineCycleRepository(cycleDao = JuanitOSDatabase.getDatabase(context).cycleDao())
    }
    override val transactionRepository: TransactionRepository by lazy {
        OfflineTransactionRepository(
            transactionDao = JuanitOSDatabase.getDatabase(context).transactionDao()
        )
    }
    override val fixedSpendingRepository: FixedSpendingRepository by lazy {
        OfflineFixedSpendingRepository(
            fixedSpendingDao = JuanitOSDatabase.getDatabase(context).fixedSpendingDao()
        )
    }
}
