package com.juanitos.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.juanitos.ui.food.FoodDestination
import com.juanitos.ui.food.FoodScreen
import com.juanitos.ui.food.FoodSettingsDestination
import com.juanitos.ui.food.FoodSettingsScreen
import com.juanitos.ui.home.HomeDestination
import com.juanitos.ui.home.HomeScreen
import com.juanitos.ui.money.MoneyDestination
import com.juanitos.ui.money.MoneyScreen

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
            FoodScreen(onNavigateUp = { navController.navigateUp() },
                onSettings = { navController.navigate(FoodSettingsDestination.route.name) })
        }
        composable(route = MoneyDestination.route.name) {
            MoneyScreen(onNavigateUp = { navController.navigateUp() })
        }
        composable(route = FoodSettingsDestination.route.name) {
            FoodSettingsScreen(onNavigateUp = { navController.navigateUp() })
        }
    }
}