package com.juanitos.ui.routes.food.batch.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
    viewModel: BatchFoodDetailsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val batchFood = viewModel.batchFood.collectAsState().value ?: return
    Scaffold(
        topBar = {
            JuanitOSTopAppBar(
                title = stringResource(BatchFoodDetailsDestination.titleRes, batchFood.name),
                canNavigateBack = true,
                navigateUp = onNavigateUp,
                actions = {
                    IconButton(onClick = { viewModel.deleteBatchFood(onNavigateUp) }) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = stringResource(R.string.delete),
                        )
                    }
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
            Text(stringResource(R.string.batch_total_grams, batchFood.totalGrams))
            Text(stringResource(R.string.batch_grams_used, batchFood.gramsUsed ?: 0))
            batchFood.ingredients.forEach { ingredient ->
                Card(
                    onClick = { onIngredient(ingredient.id) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = dimensionResource(R.dimen.padding_small))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(dimensionResource(R.dimen.padding_medium)),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(ingredient.name)
                        Text(stringResource(R.string.ingredient_grams, ingredient.grams))
                    }
                }
            }
        }
    }
}
