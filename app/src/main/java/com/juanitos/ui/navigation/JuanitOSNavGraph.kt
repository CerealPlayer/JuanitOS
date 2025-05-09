package com.juanitos.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.juanitos.ui.routes.HomeDestination
import com.juanitos.ui.routes.HomeScreen
import com.juanitos.ui.routes.food.FoodDestination
import com.juanitos.ui.routes.food.FoodScreen
import com.juanitos.ui.routes.food.batch.NewBatchFoodDestination
import com.juanitos.ui.routes.food.batch.NewBatchFoodScreen
import com.juanitos.ui.routes.food.details.FoodDetailsDestination
import com.juanitos.ui.routes.food.details.FoodDetailsScreen
import com.juanitos.ui.routes.food.ingredients.IngredientsDestination
import com.juanitos.ui.routes.food.ingredients.IngredientsScreen
import com.juanitos.ui.routes.food.ingredients.new_ingredient.NewIngredientDestination
import com.juanitos.ui.routes.food.ingredients.new_ingredient.NewIngredientScreen
import com.juanitos.ui.routes.food.new_food.NewFoodDestination
import com.juanitos.ui.routes.food.new_food.NewFoodScreen
import com.juanitos.ui.routes.food.settings.FoodSettingsDestination
import com.juanitos.ui.routes.food.settings.FoodSettingsScreen
import com.juanitos.ui.routes.money.MoneyDestination
import com.juanitos.ui.routes.money.MoneyScreen

@Composable
fun JuanitOSNavGraph(
    navController: NavHostController, modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = HomeDestination.route.route,
        modifier = modifier
    ) {
        composable(route = HomeDestination.route.route) {
            HomeScreen(onNavigateTo = { navController.navigate(it.route) })
        }
        composable(route = FoodDestination.route.route) {
            FoodScreen(
                onNavigateUp = { navController.navigateUp() },
                onSettings = { navController.navigate(FoodSettingsDestination.route.route) },
                onNewFood = { navController.navigate(NewFoodDestination.route.route) },
                onIngredients = {
                    navController.navigate(IngredientsDestination.route.route)
                },
                onNewBatchFood = {
                    navController.navigate(NewBatchFoodDestination.route.route)
                },
                onFoodDetails = {
                    navController.navigate("food/$it")
                })
        }
        composable(route = MoneyDestination.route.route) {
            MoneyScreen(onNavigateUp = { navController.navigateUp() })
        }
        composable(route = FoodSettingsDestination.route.route) {
            FoodSettingsScreen(onNavigateUp = { navController.navigateUp() })
        }
        composable(route = NewFoodDestination.route.route) {
            NewFoodScreen(onNavigateUp = { navController.navigateUp() }, onNewIngredient = {
                navController.navigate(NewIngredientDestination.route.route)
            })
        }
        composable(route = IngredientsDestination.route.route) {
            IngredientsScreen(onNavigateUp = { navController.navigateUp() }, onNewIngredient = {
                navController.navigate(NewIngredientDestination.route.route)
            })
        }
        composable(route = NewIngredientDestination.route.route) {
            NewIngredientScreen(onNavigateUp = { navController.navigateUp() })
        }
        composable(route = NewBatchFoodDestination.route.route) {
            NewBatchFoodScreen(onNavigateUp = { navController.navigateUp() }, onNewIngredient = {
                navController.navigate(NewIngredientDestination.route.route)
            })
        }
        composable(route = FoodDetailsDestination.route.route,
            arguments = listOf(
                navArgument("foodId") {
                    type = NavType.IntType
                    nullable = false
                }
            )
        ) {
            FoodDetailsScreen(
                onNavigateUp = { navController.navigateUp() },
            )
        }
    }
}