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
import com.juanitos.ui.routes.habit.HabitsDestination
import com.juanitos.ui.routes.habit.HabitsScreen
import com.juanitos.ui.routes.money.MoneyDestination
import com.juanitos.ui.routes.money.MoneyScreen
import com.juanitos.ui.routes.money.categories.CategoriesDestination
import com.juanitos.ui.routes.money.categories.CategoriesScreen
import com.juanitos.ui.routes.money.categories.NewCategoryDestination
import com.juanitos.ui.routes.money.categories.NewCategoryScreen
import com.juanitos.ui.routes.money.settings.MoneySettingsDestination
import com.juanitos.ui.routes.money.settings.MoneySettingsScreen
import com.juanitos.ui.routes.money.spendings.FixedSpendingsDestination
import com.juanitos.ui.routes.money.spendings.FixedSpendingsScreen
import com.juanitos.ui.routes.money.spendings.NewFixedSpendingDestination
import com.juanitos.ui.routes.money.spendings.NewFixedSpendingScreen
import com.juanitos.ui.routes.money.transactions.NewTransactionDestination
import com.juanitos.ui.routes.money.transactions.NewTransactionScreen
import com.juanitos.ui.routes.workout.NewWorkoutDestination
import com.juanitos.ui.routes.workout.NewWorkoutScreen
import com.juanitos.ui.routes.workout.WorkoutDestination
import com.juanitos.ui.routes.workout.WorkoutScreen
import com.juanitos.ui.routes.workout.detail.WorkoutDetailDestination
import com.juanitos.ui.routes.workout.detail.WorkoutDetailScreen
import com.juanitos.ui.routes.workout.edit.EditWorkoutDestination
import com.juanitos.ui.routes.workout.edit.EditWorkoutScreen
import com.juanitos.ui.routes.workout.exercises.ExerciseProgressDestination
import com.juanitos.ui.routes.workout.exercises.ExerciseProgressScreen
import com.juanitos.ui.routes.workout.exercises.ExercisesDestination
import com.juanitos.ui.routes.workout.exercises.ExercisesScreen
import com.juanitos.ui.routes.workout.exercises.NewExerciseDestination
import com.juanitos.ui.routes.workout.exercises.NewExerciseScreen

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
        composable(route = MoneyDestination.route.route) {
            MoneyScreen(
                onNavigateUp = { navController.navigateUp() },
                onMoneySettings = { navController.navigate(MoneySettingsDestination.route.route) },
                onNewTransaction = { navController.navigate(NewTransactionDestination.route.route) },
                onFixedSpendings = { navController.navigate(FixedSpendingsDestination.route.route) },
                onCategories = { navController.navigate(CategoriesDestination.route.route) }
            )
        }
        composable(route = MoneySettingsDestination.route.route) {
            MoneySettingsScreen(onNavigateUp = { navController.navigateUp() })
        }
        composable(route = NewTransactionDestination.route.route) {
            NewTransactionScreen(onNavigateUp = { navController.navigateUp() }, onNewCategory = {
                navController.navigate(
                    NewCategoryDestination.route.route
                )
            })
        }
        composable(route = FixedSpendingsDestination.route.route) {
            FixedSpendingsScreen(
                onNavigateUp = { navController.navigateUp() },
                onNewFixedSpending = { navController.navigate(NewFixedSpendingDestination.route.route) })
        }
        composable(route = NewFixedSpendingDestination.route.route) {
            NewFixedSpendingScreen(onNavigateUp = { navController.navigateUp() }, onNewCategory = {
                navController.navigate(
                    NewCategoryDestination.route.route
                )
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
        composable(route = WorkoutDestination.route.route) {
            WorkoutScreen(
                onNavigateUp = { navController.navigateUp() },
                onNewWorkout = { navController.navigate(NewWorkoutDestination.route.route) },
                onExercises = { navController.navigate(ExercisesDestination.route.route) },
                onWorkoutClick = { workoutId ->
                    navController.navigate("workout_detail/$workoutId")
                },
            )
        }
        composable(route = NewWorkoutDestination.route.route) {
            NewWorkoutScreen(onNavigateUp = { navController.navigateUp() })
        }
        composable(
            route = WorkoutDetailDestination.route.route,
            arguments = listOf(navArgument("workoutId") { type = NavType.IntType }),
        ) {
            WorkoutDetailScreen(
                onNavigateUp = { navController.navigateUp() },
                onEditWorkout = { workoutId ->
                    navController.navigate(EditWorkoutDestination.createRoute(workoutId))
                }
            )
        }
        composable(
            route = EditWorkoutDestination.route.route,
            arguments = listOf(navArgument("workoutId") { type = NavType.IntType }),
        ) {
            EditWorkoutScreen(onNavigateUp = { navController.navigateUp() })
        }
        composable(route = ExercisesDestination.route.route) {
            ExercisesScreen(
                onNavigateUp = { navController.navigateUp() },
                onNewExercise = { navController.navigate(NewExerciseDestination.route.route) },
                onExerciseClick = { exerciseId ->
                    navController.navigate(ExerciseProgressDestination.createRoute(exerciseId))
                }
            )
        }
        composable(
            route = ExerciseProgressDestination.route.route,
            arguments = listOf(navArgument("exerciseId") { type = NavType.IntType }),
        ) {
            ExerciseProgressScreen(onNavigateUp = { navController.navigateUp() })
        }
        composable(route = NewExerciseDestination.route.route) {
            NewExerciseScreen(onNavigateUp = { navController.navigateUp() })
        }
        composable(route = HabitsDestination.route.route) {
            HabitsScreen(onNavigateUp = { navController.navigateUp() })
        }
    }
}
