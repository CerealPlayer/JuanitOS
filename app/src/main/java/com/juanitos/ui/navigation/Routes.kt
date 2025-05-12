package com.juanitos.ui.navigation

enum class Routes(val route: String) {
    Home("home"),
    Food("food"),
    FoodSettings("food_settings"),
    NewFood("new_food"),
    Ingredients("ingredients"),
    NewIngredient("new_ingredient"),
    IngredientDetails("ingredient/{ingredientId}"),
    BatchFoods("batch_foods"),
    NewBatchFood("new_batch_food"),
    FoodDetails("food/{foodId}"),
    Money("money"),
}
