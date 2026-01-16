package com.juanitos.ui.routes.food.ingredients

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.juanitos.R
import com.juanitos.ui.AppViewModelProvider
import com.juanitos.ui.icons.Add
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
    onIngredient: (Int) -> Unit,
    viewModel: IngredientsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val ingredients = viewModel.uiState.collectAsState().value.ingredients
    Scaffold(topBar = {
        JuanitOSTopAppBar(title = stringResource(R.string.ingredients),
            canNavigateBack = true,
            navigateUp = onNavigateUp,
            actions = {
                IconButton(onNewIngredient) {
                    Add()
                }
            })
    }) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(
                top = innerPadding.calculateTopPadding(),
                bottom = innerPadding.calculateBottomPadding(),
                start = dimensionResource(R.dimen.padding_medium),
                end = dimensionResource(R.dimen.padding_medium)
            )
        ) {
            items(ingredients) { ingredient ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(dimensionResource(R.dimen.padding_small)),
                    onClick = { onIngredient(ingredient.id) },
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(dimensionResource(R.dimen.padding_medium)),
                    ) {
                        Text(
                            text = ingredient.name,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(
                                    R.string.food_ingredient_cals,
                                    ingredient.caloriesPer100
                                )
                            )
                            Text(
                                text = stringResource(
                                    R.string.food_ingredient_prot,
                                    ingredient.proteinsPer100
                                )
                            )
                        }
                    }
                }
            }
        }

    }
}