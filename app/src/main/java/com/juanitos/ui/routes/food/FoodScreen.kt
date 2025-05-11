package com.juanitos.ui.routes.food

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.juanitos.R
import com.juanitos.data.food.entities.relations.FormattedFoodDetails
import com.juanitos.ui.AppViewModelProvider
import com.juanitos.ui.navigation.JuanitOSTopAppBar
import com.juanitos.ui.navigation.NavigationDestination
import com.juanitos.ui.navigation.Routes
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    onIngredients: () -> Unit,
    onNewBatchFood: () -> Unit,
    onFoodDetails: (Int) -> Unit,
    viewModel: FoodViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val calorieLimit = viewModel.calorieLimit.collectAsState()
    val proteinLimit = viewModel.proteinLimit.collectAsState()
    val foods = viewModel.foods.collectAsState().value

    val foodCalories = foods.sumOf { it.totalCalories }
    val foodProteins = foods.sumOf { it.totalProteins }

    val caloriesLeft = (calorieLimit.value.toIntOrNull() ?: 0) - foodCalories
    val proteinsLeft = (proteinLimit.value.toDoubleOrNull() ?: 0.0) - foodProteins

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
                IconButton(onClick = onIngredients) {
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
                SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).format(Date()),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.inversePrimary)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(dimensionResource(R.dimen.padding_medium)),
                ) {
                    Text(
                        text = stringResource(
                            R.string.calories_left,
                            caloriesLeft,
                            calorieLimit.value
                        ),
                        style = MaterialTheme.typography.titleLarge
                    )
                    ProgressCircle(
                        progress = foodCalories,
                        max = calorieLimit.value.toIntOrNull() ?: 0
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(dimensionResource(R.dimen.padding_medium)),
                ) {
                    Text(
                        text = stringResource(R.string.prot_left, proteinsLeft, proteinLimit.value),
                        style = MaterialTheme.typography.titleLarge
                    )
                    ProgressCircle(
                        progress = foodProteins.toInt(),
                        max = proteinLimit.value.toIntOrNull() ?: 0
                    )
                }
            }
            foods.forEach { food ->
                FoodDetailsCard(details = food, onClick = {
                    onFoodDetails(food.id)
                })
            }
        }
    }
}

@Composable
fun FoodDetailsCard(
    details: FormattedFoodDetails,
    onClick: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Column(
            modifier = Modifier.padding(
                top = dimensionResource(R.dimen.padding_small),
                bottom = dimensionResource(R.dimen.padding_small),
            )
        ) {
            Text(
                text = details.name,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(dimensionResource(R.dimen.padding_small))
            )
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .padding(
                        start = dimensionResource(R.dimen.padding_small),
                        end = dimensionResource(R.dimen.padding_small)
                    )
                    .fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.cal_summary, details.totalCalories),
                )
                Text(
                    text = stringResource(R.string.prot_summary, details.totalProteins),
                )
            }
        }
    }
}

@Preview
@Composable
fun FoodScreenPreview() {
    FoodScreen(
        onNavigateUp = {},
        onSettings = {},
        onNewFood = {},
        onIngredients = {},
        onNewBatchFood = {},
        onFoodDetails = {},
    )
}

@Composable
fun ProgressCircle(progress: Int, max: Int) {
    val ratio = progress.toFloat() / max.toFloat()

    Row(verticalAlignment = Alignment.CenterVertically) {
        Canvas(modifier = Modifier.size(20.dp)) {
            drawCircle(Color.LightGray, style = Stroke(8.dp.toPx()))

            drawArc(
                color = Color.Green,
                startAngle = -90f,
                sweepAngle = 360 * ratio,
                useCenter = false,
                style = Stroke(8.dp.toPx())
            )
        }
    }
}