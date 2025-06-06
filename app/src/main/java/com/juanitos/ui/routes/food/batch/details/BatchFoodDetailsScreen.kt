package com.juanitos.ui.routes.food.batch.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.juanitos.R
import com.juanitos.ui.AppViewModelProvider
import com.juanitos.ui.navigation.JuanitOSTopAppBar
import com.juanitos.ui.navigation.NavigationDestination
import com.juanitos.ui.navigation.Routes

object BatchFoodDetailsDestination : NavigationDestination {
    override val route = Routes.BatchFoodDetails
    override val titleRes = R.string.batch_food
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchFoodDetailsScreen(
    onNavigateUp: () -> Unit,
    onIngredient: (Int) -> Unit,
    onEdit: (Int) -> Unit,
    viewModel: BatchFoodDetailsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val batchFood = viewModel.batchFood.collectAsState().value ?: return
    val totalCalories =
        batchFood.ingredients.sumOf { it.caloriesPer100 * it.grams / 100 }
    val totalProteins =
        batchFood.ingredients.sumOf { it.proteinsPer100 * it.grams / 100 }
    Scaffold(
        topBar = {
            JuanitOSTopAppBar(
                title = stringResource(BatchFoodDetailsDestination.titleRes, batchFood.name),
                canNavigateBack = true,
                navigateUp = onNavigateUp,
                actions = {
                    OptionsMenu(
                        onDelete = {
                            viewModel.deleteBatchFood(onNavigateUp)
                        },
                        onEdit = { onEdit(batchFood.id) }
                    )
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(
                top = padding.calculateTopPadding(),
                bottom = padding.calculateBottomPadding(),
                start = dimensionResource(R.dimen.padding_small),
                end = dimensionResource(R.dimen.padding_small),
            )
        ) {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.inversePrimary)
                    .padding(
                        dimensionResource(R.dimen.padding_small)
                    )
                    .fillMaxWidth(),
            ) {
                Text(
                    stringResource(
                        R.string.grams_ratio,
                        batchFood.gramsUsed ?: 0,
                        batchFood.totalGrams
                    ), style = MaterialTheme.typography.titleLarge
                )
                Text(stringResource(R.string.cal_summary, totalCalories))
                Text(stringResource(R.string.prot_summary, totalProteins))
            }
            batchFood.ingredients.forEach { ingredient ->
                Card(
                    onClick = { onIngredient(ingredient.ingredientId) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = dimensionResource(R.dimen.padding_small))
                ) {
                    Column(
                        modifier = Modifier
                            .padding(
                                dimensionResource(R.dimen.padding_small)
                            )
                            .fillMaxWidth()
                    ) {

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(ingredient.name)
                            Text(stringResource(R.string.ingredient_grams, ingredient.grams))
                        }
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                stringResource(
                                    R.string.cal_summary,
                                    ingredient.caloriesPer100 * ingredient.grams / 100
                                )
                            )
                            Text(
                                stringResource(
                                    R.string.prot_summary,
                                    ingredient.proteinsPer100 * ingredient.grams / 100
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OptionsMenu(
    onDelete: () -> Unit,
    onEdit: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = Modifier.padding(dimensionResource(R.dimen.padding_small))) {
        IconButton(onClick = { expanded = !expanded }) {
            Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = stringResource(R.string.settings),
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.edit)) }, onClick = {
                    onEdit()
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.delete)) }, onClick = {
                    onDelete()
                    expanded = false
                }
            )
        }
    }
}