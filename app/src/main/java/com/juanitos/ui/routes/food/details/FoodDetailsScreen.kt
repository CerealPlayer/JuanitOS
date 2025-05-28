package com.juanitos.ui.routes.food.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
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
import com.juanitos.data.food.entities.relations.FoodIngredientDetails
import com.juanitos.ui.AppViewModelProvider
import com.juanitos.ui.commons.food.BatchFoodEntry
import com.juanitos.ui.commons.food.BatchFoodEntryCard
import com.juanitos.ui.commons.food.IngredientEntry
import com.juanitos.ui.commons.food.IngredientEntryCard
import com.juanitos.ui.navigation.JuanitOSTopAppBar
import com.juanitos.ui.navigation.NavigationDestination
import com.juanitos.ui.navigation.Routes

object FoodDetailsDestination : NavigationDestination {
    override val route: Routes = Routes.FoodDetails
    override val titleRes = R.string.food_detail_name
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodDetailsScreen(
    onNavigateUp: () -> Unit,
    viewModel: FoodDetailsViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val food = viewModel.food.collectAsState().value
        ?: return

    val foodDetails = food.toFormattedFoodDetails()
    Scaffold(topBar = {
        JuanitOSTopAppBar(
            title = stringResource(FoodDetailsDestination.titleRes, foodDetails.name),
            navigateUp = onNavigateUp,
            canNavigateBack = true,
            actions = {
                OptionsMenu(
                    onDelete = {},
                    onEdit = {}
                )
            })
    }) { paddingValues ->
        Column(
            modifier = Modifier.padding(
                start = dimensionResource(R.dimen.padding_medium),
                end = dimensionResource(R.dimen.padding_medium),
                bottom = paddingValues.calculateBottomPadding(),
                top = paddingValues.calculateTopPadding()
            ),
            verticalArrangement = Arrangement.spacedBy(
                dimensionResource(id = R.dimen.padding_small)
            )
        ) {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.inversePrimary)
                    .padding(dimensionResource(R.dimen.padding_small))
                    .fillMaxWidth(),
            ) {
                Text(stringResource(R.string.cal_summary, foodDetails.totalCalories))
                Text(stringResource(R.string.prot_summary, foodDetails.totalProteins))
            }
            food.foodIngredients.forEach { foodIngredient ->
                FoodIngredientCard(foodIngredient = foodIngredient)
            }
        }
    }
}

@Composable
fun FoodIngredientCard(foodIngredient: FoodIngredientDetails) {
    val ingredient = foodIngredient.ingredient
    if (ingredient != null) {
        IngredientEntryCard(
            ingredientEntry = IngredientEntry(
                ingredient = ingredient,
                qt = foodIngredient.foodIngredient.grams
            )
        )
    }
    val batchFoodIngredient = foodIngredient.batchFood
    if (batchFoodIngredient != null) {
        BatchFoodEntryCard(
            batchFoodEntry = BatchFoodEntry(
                qt = foodIngredient.foodIngredient.grams,
                batchFood = batchFoodIngredient.toBatchFoodWithIngredientDetails()
            )
        )
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