package com.juanitos.ui

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.juanitos.JuanitOSApplication
import com.juanitos.ui.routes.food.FoodViewModel
import com.juanitos.ui.routes.food.batch.BatchFoodsViewModel
import com.juanitos.ui.routes.food.batch.details.BatchFoodDetailsViewModel
import com.juanitos.ui.routes.food.batch.edit.EditBatchFoodViewModel
import com.juanitos.ui.routes.food.batch.new_batch.NewBatchFoodViewModel
import com.juanitos.ui.routes.food.details.FoodDetailsViewModel
import com.juanitos.ui.routes.food.ingredients.IngredientsViewModel
import com.juanitos.ui.routes.food.ingredients.details.IngredientDetailsViewModel
import com.juanitos.ui.routes.food.ingredients.new_ingredient.NewIngredientViewModel
import com.juanitos.ui.routes.food.new_food.NewFoodViewModel
import com.juanitos.ui.routes.food.settings.FoodSettingsViewModel
import com.juanitos.ui.routes.money.MoneyViewModel
import com.juanitos.ui.routes.money.transactions.NewTransactionViewModel

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
                juanitOSApplication().container.batchFoodRepository,
                juanitOSApplication().container.foodRepository,
                juanitOSApplication().container.foodIngredientRepository
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
        initializer {
            FoodDetailsViewModel(
                savedStateHandle = this.createSavedStateHandle(),
                juanitOSApplication().container.foodRepository,
                juanitOSApplication().container.batchFoodRepository
            )
        }
        initializer {
            IngredientsViewModel(
                juanitOSApplication().container.ingredientRepository
            )
        }
        initializer {
            IngredientDetailsViewModel(
                savedStateHandle = this.createSavedStateHandle(),
                juanitOSApplication().container.ingredientRepository
            )
        }
        initializer {
            BatchFoodsViewModel(
                juanitOSApplication().container.batchFoodRepository
            )
        }
        initializer {
            BatchFoodDetailsViewModel(
                savedStateHandle = this.createSavedStateHandle(),
                juanitOSApplication().container.batchFoodRepository
            )
        }
        initializer {
            EditBatchFoodViewModel(
                savedStateHandle = this.createSavedStateHandle(),
                juanitOSApplication().container.ingredientRepository,
                juanitOSApplication().container.batchFoodRepository,
                juanitOSApplication().container.batchFoodIngredientRepository,
            )
        }
        initializer {
            MoneyViewModel(
                juanitOSApplication().container.cycleRepository
            )
        }
        initializer {
            com.juanitos.ui.routes.money.settings.MoneySettingsViewModel(
                juanitOSApplication().container.cycleRepository
            )
        }
        initializer {
            NewTransactionViewModel(
                juanitOSApplication().container.transactionRepository,
                juanitOSApplication().container.cycleRepository
            )
        }
    }
}

fun CreationExtras.juanitOSApplication(): JuanitOSApplication =
    (this[AndroidViewModelFactory.APPLICATION_KEY] as JuanitOSApplication)
