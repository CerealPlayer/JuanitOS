package com.juanitos.ui

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.juanitos.JuanitOSApplication
import com.juanitos.ui.food.FoodSettingsViewModel
import com.juanitos.ui.food.FoodViewModel
import com.juanitos.ui.food.NewBatchFoodViewModel
import com.juanitos.ui.food.NewFoodViewModel
import com.juanitos.ui.food.NewIngredientViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            FoodSettingsViewModel(juanitOSApplication().container.settingsRepository)
        }
        initializer {
            FoodViewModel(
                juanitOSApplication().container.settingsRepository,
                juanitOSApplication().container.foodRepository
            )
        }
        initializer {
            NewFoodViewModel(
                juanitOSApplication().container.ingredientRepository,
                juanitOSApplication().container.foodRepository,
                juanitOSApplication().container.foodIngredientRepository,
            )
        }
        initializer {
            NewIngredientViewModel(
                juanitOSApplication().container.ingredientRepository
            )
        }
        initializer {
            NewBatchFoodViewModel(
                juanitOSApplication().container.ingredientRepository,
                juanitOSApplication().container.batchFoodRepository,
                juanitOSApplication().container.batchFoodIngredientRepository,
            )
        }
    }
}

fun CreationExtras.juanitOSApplication(): JuanitOSApplication =
    (this[AndroidViewModelFactory.APPLICATION_KEY] as JuanitOSApplication)