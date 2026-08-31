package com.juanitos.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.juanitos.ui.routes.money.MoneyDestination
import com.juanitos.ui.routes.money.MoneyScreen
import com.juanitos.ui.routes.money.accounts.AccountsDestination
import com.juanitos.ui.routes.money.accounts.AccountsScreen
import com.juanitos.ui.routes.money.accounts.NewAccountDestination
import com.juanitos.ui.routes.money.accounts.NewAccountScreen
import com.juanitos.ui.routes.money.categories.CategoriesDestination
import com.juanitos.ui.routes.money.categories.CategoriesScreen
import com.juanitos.ui.routes.money.categories.NewCategoryDestination
import com.juanitos.ui.routes.money.categories.NewCategoryScreen
import com.juanitos.ui.routes.money.log.LogDestination
import com.juanitos.ui.routes.money.log.LogScreen
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
                onAccounts = { navController.navigate(AccountsDestination.route.route) },
                onNewTransaction = { navController.navigate(NewTransactionDestination.route.route) },
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
        composable(route = AccountsDestination.route.route) {
            AccountsScreen(
                onNavigateUp = { navController.navigateUp() },
                onNewAccount = { navController.navigate(NewAccountDestination.route.route) }
            )
        }
        composable(route = NewAccountDestination.route.route) {
            NewAccountScreen(onNavigateUp = { navController.navigateUp() })
        }
        composable(route = NewTransactionDestination.route.route) {
            NewTransactionScreen(onNavigateUp = { navController.navigateUp() }, onNewCategory = {
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
