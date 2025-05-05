package com.juanitos.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.juanitos.ui.routes.HomeDestination
import com.juanitos.ui.routes.HomeScreen
import com.juanitos.ui.routes.food.FoodDestination
import com.juanitos.ui.routes.food.FoodScreen
import com.juanitos.ui.routes.food.batch.NewBatchFoodDestination
import com.juanitos.ui.routes.food.batch.NewBatchFoodScreen
import com.juanitos.ui.routes.food.ingredient.NewIngredientDestination
import com.juanitos.ui.routes.food.ingredient.NewIngredientScreen
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
        startDestination = HomeDestination.route.name,
        modifier = modifier
    ) {
        composable(route = HomeDestination.route.name) {
            HomeScreen(onNavigateTo = { navController.navigate(it.name) })
        }
        composable(route = FoodDestination.route.name) {
            FoodScreen(
                onNavigateUp = { navController.navigateUp() },
                onSettings = { navController.navigate(FoodSettingsDestination.route.name) },
                onNewFood = { navController.navigate(NewFoodDestination.route.name) },
                onNewIngredient = {
                    navController.navigate(NewIngredientDestination.route.name)
                },
                onNewBatchFood = {
                    navController.navigate(NewBatchFoodDestination.route.name)
                })
        }
        composable(route = MoneyDestination.route.name) {
            MoneyScreen(onNavigateUp = { navController.navigateUp() })
        }
        composable(route = FoodSettingsDestination.route.name) {
            FoodSettingsScreen(onNavigateUp = { navController.navigateUp() })
        }
        composable(route = NewFoodDestination.route.name) {
            NewFoodScreen(onNavigateUp = { navController.navigateUp() }, onNewIngredient = {
                navController.navigate(NewIngredientDestination.route.name)
            })
        }
        composable(route = NewIngredientDestination.route.name) {
            NewIngredientScreen(onNavigateUp = { navController.navigateUp() })
        }
        composable(route = NewBatchFoodDestination.route.name) {
            NewBatchFoodScreen(onNavigateUp = { navController.navigateUp() }, onNewIngredient = {
                navController.navigate(NewIngredientDestination.route.name)
            })
        }
    }
}