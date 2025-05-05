package com.juanitos.ui.routes.food

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.juanitos.R
import com.juanitos.data.food.entities.relations.FoodWithIngredients
import com.juanitos.ui.AppViewModelProvider
import com.juanitos.ui.navigation.JuanitOSTopAppBar
import com.juanitos.ui.navigation.NavigationDestination
import com.juanitos.ui.navigation.Routes

object FoodDestination : NavigationDestination {
    override val route = Routes.Food
    override val titleRes = R.string.food
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodScreen(
    onNavigateUp: () -> Unit,
    onSettings: () -> Unit,
    onNewFood: () -> Unit,
    onNewIngredient: () -> Unit,
    onNewBatchFood: () -> Unit,
    viewModel: FoodViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val calorieLimit = viewModel.calorieLimit.collectAsState()
    val proteinLimit = viewModel.proteinLimit.collectAsState()
    val foods = viewModel.todaysFoods.collectAsState()

    val totalCalories = foods.value.sumOf { food ->
        food.ingredients.sumOf {
            (it.grams.toIntOrNull() ?: 0) * (it.ingredient.caloriesPer100.toIntOrNull() ?: 0) / 100
        }
    }
    val totalProteins = foods.value.sumOf { food ->
        food.ingredients.sumOf {
            (it.grams.toIntOrNull() ?: 0) * (it.ingredient.proteinsPer100.toIntOrNull() ?: 0) / 100
        }
    }

    val caloriesLeft = (calorieLimit.value.toIntOrNull() ?: 0) - totalCalories
    val proteinsLeft = (proteinLimit.value.toIntOrNull() ?: 0) - totalProteins

    Scaffold(topBar = {
        JuanitOSTopAppBar(title = stringResource(FoodDestination.titleRes),
            canNavigateBack = true,
            navigateUp = onNavigateUp,
            actions = {
                IconButton(onClick = onSettings) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = stringResource(R.string.settings)
                    )
                }
            })
    }, bottomBar = {
        BottomAppBar(
            actions = {
                IconButton(onClick = onNewIngredient) {
                    Icon(
                        painter = painterResource(R.drawable.ingredient),
                        contentDescription = stringResource(R.string.new_ingredient)
                    )
                }
                IconButton(onClick = onNewBatchFood) {
                    Icon(
                        painter = painterResource(R.drawable.batch),
                        contentDescription = stringResource(R.string.new_batch_food)
                    )
                }
            },

            floatingActionButton = {
                FloatingActionButton(onClick = onNewFood) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = stringResource(R.string.new_food)
                    )
                }
            }
        )
    }) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(
                    top = innerPadding.calculateTopPadding(),
                    bottom = innerPadding.calculateBottomPadding(),
                    start = dimensionResource(R.dimen.padding_small),
                    end = dimensionResource(R.dimen.padding_small)
                )
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
        ) {
            Text(
                text = stringResource(R.string.calories_left, caloriesLeft),
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = stringResource(R.string.prot_left, proteinsLeft),
                style = MaterialTheme.typography.titleMedium
            )
            foods.value.forEach { food ->
                FoodEntry(entry = food)
            }
        }
    }
}

@Composable
fun FoodEntry(
    entry: FoodWithIngredients
) {
    val totalCal = entry.ingredients.sumOf {
        (it.grams.toIntOrNull() ?: 0) * (it.ingredient.caloriesPer100.toIntOrNull() ?: 0) / 100
    }
    val totalProt = entry.ingredients.sumOf {
        (it.grams.toIntOrNull() ?: 0) * (it.ingredient.proteinsPer100.toIntOrNull() ?: 0) / 100
    }
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(dimensionResource(R.dimen.padding_small))) {
            Text(text = entry.food.name)
            Text(text = stringResource(R.string.food_summary, totalCal, totalProt))
            Text(text = stringResource(R.string.n_ingredients, entry.ingredients.size))
        }
    }
}

@Preview
@Composable
fun FoodScreenPreview() {
    FoodScreen(onNavigateUp = {}, onSettings = {}, onNewFood = {}, onNewIngredient = {}, onNewBatchFood = {}
    )
}

