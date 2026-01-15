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
    BatchFoodDetails("batch_food/{batchFoodId}"),
    EditBatchFood("edit_batch_food/{batchFoodId}"),
    FoodDetails("food/{foodId}"),
    Track("track"),
    Money("money"),
    MoneySettings("money_settings"),
    NewTransaction("new_transaction"),
    NewFixedSpending("new_fixed_spending");

    fun createBatchFoodDetailsRoute(batchFoodId: Int) = "batch_food/$batchFoodId"
    fun createIngredientDetailsRoute(ingredientId: Int) = "ingredient/$ingredientId"
    fun createFoodDetailsRoute(foodId: Int) = "food/$foodId"
    fun createEditBatchFoodRoute(batchFoodId: Int) = "edit_batch_food/$batchFoodId"
}
