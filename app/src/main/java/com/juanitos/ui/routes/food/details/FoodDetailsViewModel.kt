package com.juanitos.ui.routes.food.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

class FoodDetailsViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    val foodId =
        savedStateHandle.get<Int>("foodId") ?: throw IllegalArgumentException("foodId not found")
}