package com.juanitos.ui

import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.juanitos.ui.food.FoodSettingsViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            FoodSettingsViewModel()
        }
    }
}