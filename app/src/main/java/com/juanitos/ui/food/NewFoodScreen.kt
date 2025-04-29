package com.juanitos.ui.food

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
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
import com.juanitos.data.Ingredient
import com.juanitos.ui.AppViewModelProvider
import com.juanitos.ui.commons.FormColumn
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
    viewModel: NewFoodViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val query = viewModel.ingredientQuery.collectAsState()
    val ingredients = viewModel.ingredientSuggestions.collectAsState()
    val selected = viewModel.currentSelected.collectAsState()
    val uiState = viewModel.newFoodUiState

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
            IngredientSearch(
                query = query.value,
                expanded = viewModel.searchExpanded,
                onQueryChange = { viewModel.onQueryChange(it) },
                onExpandedChange = { viewModel.onExpandedChange(it) },
                onSearch = { viewModel.onSearch(it) },
                ingredientSearch = ingredients.value,
            )
            if (viewModel.newIngredientOpen) {
                NewIngredientDialog(onDismissRequest = { viewModel.onNewIngredientClose() },
                    onSave = { viewModel.onNewIngredientSave() },
                    newIngredientUiState = viewModel.newIngredientUiState,
                    onNewIngredientChange = { name, kcal, protein ->
                        viewModel.onNewIngredientChange(name, kcal, protein)
                    })
            }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientSearch(
    query: String,
    expanded: Boolean,
    onQueryChange: (String) -> Unit,
    onExpandedChange: (Boolean) -> Unit,
    onSearch: (String) -> Unit,
    ingredientSearch: List<Ingredient>
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(dimensionResource(R.dimen.padding_small))
    ) {
        SearchBar(
            inputField = {
                SearchBarDefaults.InputField(
                    query = query,
                    onQueryChange = onQueryChange,
                    onSearch = onSearch,
                    expanded = expanded,
                    onExpandedChange = onExpandedChange,
                    placeholder = { Text(stringResource(R.string.search_ingredient)) },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = null,
                            modifier = Modifier.padding(dimensionResource(R.dimen.padding_small))
                        )
                    },
                )
            },
            expanded = expanded,
            onExpandedChange = onExpandedChange,
        ) {
            Column {
                ListItem(headlineContent = { Text(stringResource(R.string.add_ingredient)) },
                    modifier = Modifier
                        .clickable {
                            onQueryChange("new_ingredient")
                            onExpandedChange(false)
                        }
                        .fillMaxWidth())
                ingredientSearch.forEach { ingredient ->
                    ListItem(headlineContent = { Text(ingredient.name) },
                        modifier = Modifier
                            .clickable {
                                onQueryChange(ingredient.name)
                                onExpandedChange(false)
                            }
                            .fillMaxWidth())
                }
            }
        }
    }
}

@Composable
fun NewIngredientDialog(
    onDismissRequest: () -> Unit,
    newIngredientUiState: NewIngredientUiState,
    onNewIngredientChange: (String, String, String) -> Unit,
    onSave: (String) -> Unit,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
    ) {
        Card {
            FormColumn(innerPadding = PaddingValues(dimensionResource(R.dimen.padding_small))) {
                OutlinedTextField(value = newIngredientUiState.name,
                    onValueChange = {
                        onNewIngredientChange(
                            it, newIngredientUiState.kcal, newIngredientUiState.protein
                        )
                    },
                    label = { Text(stringResource(R.string.ingredient_name)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(value = newIngredientUiState.kcal,
                    onValueChange = {
                        onNewIngredientChange(
                            newIngredientUiState.name, it, newIngredientUiState.protein
                        )
                    },
                    label = { Text(stringResource(R.string.ingredient_calories)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(value = newIngredientUiState.protein,
                    onValueChange = {
                        onNewIngredientChange(
                            newIngredientUiState.name, newIngredientUiState.kcal, it
                        )
                    },
                    label = { Text(stringResource(R.string.ingredient_protein)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Button(
                    onClick = {
                        onSave(newIngredientUiState.name)
                        onDismissRequest()
                    }, modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.save))
                }
                Button(
                    onClick = onDismissRequest,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        }
    }
}

@Composable
fun IngredientQtDialog(
    name: String,
    onDismissRequest: () -> Unit,
    qt: String,
    onQtChange: (String) -> Unit,
    onSave: () -> Unit,
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Card {
            Column(
                modifier = Modifier
                    .padding(dimensionResource(R.dimen.padding_medium))
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(dimensionResource(R.dimen.padding_medium)),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = name, style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = qt,
                        onValueChange = onQtChange,
                        label = { Text(stringResource(R.string.qt)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.width(dimensionResource(R.dimen.input_width)),
                        singleLine = true
                    )
                }
                Button(
                    onClick = onSave, modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.save))
                }
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