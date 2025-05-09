package com.juanitos.ui.routes.food.ingredients

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.juanitos.R
import com.juanitos.ui.AppViewModelProvider
import com.juanitos.ui.navigation.JuanitOSTopAppBar
import com.juanitos.ui.navigation.NavigationDestination
import com.juanitos.ui.navigation.Routes

object IngredientsDestination : NavigationDestination {
    override val route = Routes.Ingredients
    override val titleRes = R.string.ingredients
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientsScreen(
    onNavigateUp: () -> Unit,
    onNewIngredient: () -> Unit,
    viewModel: IngredientsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val ingredients = viewModel.uiState.collectAsState().value.ingredients
    Scaffold(topBar = {
        JuanitOSTopAppBar(title = stringResource(R.string.ingredients),
            canNavigateBack = true,
            navigateUp = onNavigateUp,
            actions = {
                IconButton(onNewIngredient) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = stringResource(R.string.new_ingredient),
                    )
                }
            })
    }) { innerPadding ->
        Column(
            modifier = Modifier.padding(
                top = innerPadding.calculateTopPadding(),
                bottom = innerPadding.calculateBottomPadding(),
                start = dimensionResource(R.dimen.padding_medium),
                end = dimensionResource(R.dimen.padding_medium)
            )
        ) {
            ingredients.forEach { ingredient ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(dimensionResource(R.dimen.padding_small)),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(dimensionResource(R.dimen.padding_medium)),
                        verticalArrangement = Arrangement.spacedBy(
                            dimensionResource(id = R.dimen.padding_medium)
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = ingredient.name,
                                style = MaterialTheme.typography.titleMedium
                            )
                            IconButton(onClick = { viewModel.deleteIngredient(ingredient) }) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = stringResource(R.string.delete)
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(
                                    R.string.food_ingredient_cals,
                                    ingredient.caloriesPer100.toIntOrNull() ?: 0
                                )
                            )
                            Text(
                                text = stringResource(
                                    R.string.food_ingredient_prot,
                                    ingredient.proteinsPer100.toIntOrNull() ?: 0
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}