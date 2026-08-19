package com.juanitos.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.juanitos.ui.routes.money.MoneyDestination
import com.juanitos.ui.routes.money.MoneyScreen
import com.juanitos.ui.routes.money.categories.CategoriesDestination
import com.juanitos.ui.routes.money.categories.CategoriesScreen
import com.juanitos.ui.routes.money.categories.NewCategoryDestination
import com.juanitos.ui.routes.money.categories.NewCategoryScreen
import com.juanitos.ui.routes.money.log.LogDestination
import com.juanitos.ui.routes.money.log.LogScreen
import com.juanitos.ui.routes.money.settings.MoneySettingsDestination
import com.juanitos.ui.routes.money.settings.MoneySettingsScreen
import com.juanitos.ui.routes.money.spendings.EditFixedSpendingDestination
import com.juanitos.ui.routes.money.spendings.EditFixedSpendingScreen
import com.juanitos.ui.routes.money.spendings.FixedSpendingsDestination
import com.juanitos.ui.routes.money.spendings.FixedSpendingsScreen
import com.juanitos.ui.routes.money.spendings.NewFixedSpendingDestination
import com.juanitos.ui.routes.money.spendings.NewFixedSpendingScreen
import com.juanitos.ui.routes.money.stats.MoneyStatsDestination
import com.juanitos.ui.routes.money.stats.MoneyStatsScreen
import com.juanitos.ui.routes.money.transactions.NewTransactionDestination
import com.juanitos.ui.routes.money.transactions.NewTransactionScreen

@Composable
fun JuanitOSNavGraph(
    navController: NavHostController, modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = MoneyDestination.route.route,
        modifier = modifier
    ) {
        composable(route = MoneyDestination.route.route) {
            MoneyScreen(
                onMoneySettings = { navController.navigate(MoneySettingsDestination.route.route) },
                onNewTransaction = { navController.navigate(NewTransactionDestination.route.route) },
                onFixedSpendings = { navController.navigate(FixedSpendingsDestination.route.route) },
                onCategories = { navController.navigate(CategoriesDestination.route.route) },
                onMoneyStats = { navController.navigate(MoneyStatsDestination.route.route) },
                onLog = { navController.navigate(LogDestination.route.route) },
            )
        }
        composable(route = LogDestination.route.route) {
            LogScreen(onNavigateUp = { navController.navigateUp() })
        }
        composable(route = MoneyStatsDestination.route.route) {
            MoneyStatsScreen(onNavigateUp = { navController.navigateUp() })
        }
        composable(route = MoneySettingsDestination.route.route) {
            MoneySettingsScreen(onNavigateUp = { navController.navigateUp() })
        }
        composable(route = NewTransactionDestination.route.route) {
            NewTransactionScreen(onNavigateUp = { navController.navigateUp() }, onNewCategory = {
                navController.navigate(NewCategoryDestination.route.route)
            })
        }
        composable(route = FixedSpendingsDestination.route.route) {
            FixedSpendingsScreen(
                onNavigateUp = { navController.navigateUp() },
                onNewFixedSpending = { navController.navigate(NewFixedSpendingDestination.route.route) },
                onEditFixedSpending = { id ->
                    navController.navigate(EditFixedSpendingDestination.routeWithId(id))
                })
        }
        composable(route = NewFixedSpendingDestination.route.route) {
            NewFixedSpendingScreen(onNavigateUp = { navController.navigateUp() }, onNewCategory = {
                navController.navigate(NewCategoryDestination.route.route)
            })
        }
        composable(
            route = EditFixedSpendingDestination.route.route,
            arguments = listOf(navArgument("fixedSpendingId") { type = NavType.IntType })
        ) {
            EditFixedSpendingScreen(onNavigateUp = { navController.navigateUp() }, onNewCategory = {
                navController.navigate(NewCategoryDestination.route.route)
            })
        }
        composable(route = CategoriesDestination.route.route) {
            CategoriesScreen(
                onNavigateUp = { navController.navigateUp() },
                onNewCategory = { navController.navigate(NewCategoryDestination.route.route) }
            )
        }
        composable(route = NewCategoryDestination.route.route) {
            NewCategoryScreen(onNavigateUp = { navController.navigateUp() })
        }
    }
}
