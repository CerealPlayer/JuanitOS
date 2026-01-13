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
import com.juanitos.ui.routes.food.batch.BatchFoodsDestination
import com.juanitos.ui.routes.food.batch.BatchFoodsScreen
import com.juanitos.ui.routes.food.batch.details.BatchFoodDetailsDestination
import com.juanitos.ui.routes.food.batch.details.BatchFoodDetailsScreen
import com.juanitos.ui.routes.food.batch.edit.EditBatchFoodDestination
import com.juanitos.ui.routes.food.batch.edit.EditBatchFoodScreen
import com.juanitos.ui.routes.food.batch.new_batch.NewBatchFoodDestination
import com.juanitos.ui.routes.food.batch.new_batch.NewBatchFoodScreen
import com.juanitos.ui.routes.food.details.FoodDetailsDestination
import com.juanitos.ui.routes.food.details.FoodDetailsScreen
import com.juanitos.ui.routes.food.ingredients.IngredientsDestination
import com.juanitos.ui.routes.food.ingredients.IngredientsScreen
import com.juanitos.ui.routes.food.ingredients.details.IngredientDetailsDestination
import com.juanitos.ui.routes.food.ingredients.details.IngredientDetailsScreen
import com.juanitos.ui.routes.food.ingredients.new_ingredient.NewIngredientDestination
import com.juanitos.ui.routes.food.ingredients.new_ingredient.NewIngredientScreen
import com.juanitos.ui.routes.food.new_food.NewFoodDestination
import com.juanitos.ui.routes.food.new_food.NewFoodScreen
import com.juanitos.ui.routes.food.settings.FoodSettingsDestination
import com.juanitos.ui.routes.food.settings.FoodSettingsScreen
import com.juanitos.ui.routes.money.MoneyDestination
import com.juanitos.ui.routes.money.MoneyScreen
import com.juanitos.ui.routes.money.settings.MoneySettingsDestination
import com.juanitos.ui.routes.money.settings.MoneySettingsScreen
import com.juanitos.ui.routes.money.spendings.NewFixedSpendingDestination
import com.juanitos.ui.routes.money.spendings.NewFixedSpendingScreen
import com.juanitos.ui.routes.money.transactions.NewTransactionDestination
import com.juanitos.ui.routes.money.transactions.NewTransactionScreen

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
                onNewFood = { navController.navigate(NewFoodDestination.route.route) },
                onIngredients = {
                    navController.navigate(IngredientsDestination.route.route)
                },
                onBatchFood = {
                    navController.navigate(BatchFoodsDestination.route.route)
                },
                onFoodDetails = {
                    navController.navigate(FoodDetailsDestination.route.createFoodDetailsRoute(it))
                })
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
            }, onIngredient = {
                navController.navigate(
                    IngredientDetailsDestination.route.createIngredientDetailsRoute(
                        it
                    )
                )
            })
        }
        composable(route = NewIngredientDestination.route.route) {
            NewIngredientScreen(onNavigateUp = { navController.navigateUp() })
        }
        composable(route = BatchFoodsDestination.route.route) {
            BatchFoodsScreen(onNavigateUp = { navController.navigateUp() }, onNewBatchFood = {
                navController.navigate(NewBatchFoodDestination.route.route)
            }, onBatchFoodDetails = {
                navController.navigate(BatchFoodsDestination.route.createBatchFoodDetailsRoute(it))
            })
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
        composable(route = IngredientDetailsDestination.route.route,
            arguments = listOf(
                navArgument("ingredientId") {
                    type = NavType.StringType
                    nullable = false
                }
            )
        ) {
            IngredientDetailsScreen(
                navigateUp = { navController.navigateUp() },
            )
        }
        composable(route = BatchFoodDetailsDestination.route.route,
            arguments = listOf(
                navArgument("batchFoodId") {
                    type = NavType.IntType
                    nullable = false
                }
            )
        ) {
            BatchFoodDetailsScreen(
                onNavigateUp = { navController.navigateUp() },
                onIngredient = {
                    navController.navigate(
                        IngredientDetailsDestination.route.createIngredientDetailsRoute(
                            it
                        )
                    )
                },
                onEdit = {
                    navController.navigate(
                        EditBatchFoodDestination.route.createEditBatchFoodRoute(it)
                    )
                }
            )
        }
        composable(route = EditBatchFoodDestination.route.route, arguments = listOf(
            navArgument("batchFoodId") {
                type = NavType.IntType
                nullable = false
            }
        )) {
            EditBatchFoodScreen(
                onNavigateUp = { navController.navigateUp() },
                onNewIngredient = {
                    navController.navigate(NewIngredientDestination.route.route)
                }
            )
        }
        composable(route = MoneyDestination.route.route) {
            MoneyScreen(
                onNavigateUp = { navController.navigateUp() },
                onMoneySettings = { navController.navigate(MoneySettingsDestination.route.route) },
                onNewTransaction = { navController.navigate(NewTransactionDestination.route.route) },
                onNewFixedSpending = { navController.navigate(NewFixedSpendingDestination.route.route) }
            )
        }
        composable(route = MoneySettingsDestination.route.route) {
            MoneySettingsScreen(onNavigateUp = { navController.navigateUp() })
        }
        composable(route = NewTransactionDestination.route.route) {
            NewTransactionScreen(onNavigateUp = { navController.navigateUp() })
        }
        composable(route = NewFixedSpendingDestination.route.route) {
            NewFixedSpendingScreen(onNavigateUp = { navController.navigateUp() })
        }
    }
}
