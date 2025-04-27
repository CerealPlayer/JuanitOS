package com.juanitos.ui

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.juanitos.ui.food.FoodSettingsViewModel
import com.juanitos.JuanitOSApplication

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            FoodSettingsViewModel(inventoryApplication().container.settingsRepository)
        }
    }
}

fun CreationExtras.inventoryApplication(): JuanitOSApplication =
    (this[AndroidViewModelFactory.APPLICATION_KEY] as JuanitOSApplication)