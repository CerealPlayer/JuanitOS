package com.juanitos.ui.food

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.juanitos.R
import com.juanitos.data.food.entities.Ingredient
import com.juanitos.ui.AppViewModelProvider
import com.juanitos.ui.commons.FormColumn
import com.juanitos.ui.commons.IngredientQtDialog
import com.juanitos.ui.commons.Search
import com.juanitos.ui.navigation.JuanitOSTopAppBar
import com.juanitos.ui.navigation.NavigationDestination
import com.juanitos.ui.navigation.Routes

object NewFoodDestination : NavigationDestination {
    override val route = Routes.NewFood
    override val titleRes = R.string.new_food
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewFoodScreen(
    onNavigateUp: () -> Unit,
    onNewIngredient: () -> Unit,
    viewModel: NewFoodViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val query = viewModel.ingredientQuery.collectAsState()
    val ingredients = viewModel.ingredientSuggestions.collectAsState()
    val selected = viewModel.currentSelected.collectAsState()
    val uiState = viewModel.newFoodUiState

    val newIngString = stringResource(R.string.new_ingredient)

    Scaffold(topBar = {
        JuanitOSTopAppBar(
            title = stringResource(NewFoodDestination.titleRes),
            canNavigateBack = true,
            navigateUp = onNavigateUp,
        )
    }, floatingActionButton = {
        FloatingActionButton(
            onClick = { viewModel.onSaveFoodOpenChange(true) },
        ) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = stringResource(R.string.save),
            )
        }
    }) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Search(
                query = query.value,
                expanded = viewModel.searchExpanded,
                onQueryChange = {
                    if (it == newIngString) {
                        onNewIngredient()
                    } else {
                        viewModel.onQueryChange(it)
                    }
                },
                onExpandedChange = { viewModel.onExpandedChange(it) },
                onSearch = { viewModel.onSearch(it) },
                items = listOf(newIngString) + ingredients.value.map { it.name },
            )
            if (selected.value != null) {
                IngredientQtDialog(name = selected.value!!.name,
                    onDismissRequest = { viewModel.onIngredientQtDismiss() },
                    qt = viewModel.ingredientQt,
                    onQtChange = { viewModel.onIngredientQtChange(it) },
                    onSave = {
                        viewModel.onIngredientQtSave()
                    })
            }
            uiState.foodIngredients.forEach { foodIngredient ->
                FoodIngredientSummary(
                    ingredient = foodIngredient.ingredient,
                    quantity = foodIngredient.quantity,
                )
            }
            if (viewModel.saveFoodOpen) {
                SaveFoodDialog(name = viewModel.newFoodName,
                    onNameChange = { viewModel.onNewFoodNameChange(it) },
                    onDismissRequest = { viewModel.onSaveFoodOpenChange(false) },
                    onSave = {
                        viewModel.onNewFoodSave(onNavigateUp)
                    })
            }
        }
    }
}

@Composable
fun FoodIngredientSummary(
    ingredient: Ingredient,
    quantity: String,
) {
    val qtInt = quantity.toIntOrNull() ?: 0
    val cals = ingredient.caloriesPer100.toIntOrNull() ?: 0
    val prots = ingredient.proteinsPer100.toIntOrNull() ?: 0
    val totalCals = (qtInt * cals) / 100
    val totalProts = (qtInt * prots) / 100
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(dimensionResource(R.dimen.padding_medium)),
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
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = ingredient.name, style = MaterialTheme.typography.titleMedium)
                Text(text = stringResource(R.string.food_ingredient_qt, qtInt))
            }
            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = stringResource(R.string.food_ingredient_cals, totalCals))
                Text(text = stringResource(R.string.food_ingredient_prot, totalProts))
            }
        }
    }
}

@Composable
fun SaveFoodDialog(
    name: String, onNameChange: (String) -> Unit, onDismissRequest: () -> Unit, onSave: () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Card {
            FormColumn(innerPadding = PaddingValues(dimensionResource(R.dimen.padding_small))) {
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text(stringResource(R.string.food_name)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Button(
                    onClick = onSave, modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.save))
                }
            }
        }
    }
}