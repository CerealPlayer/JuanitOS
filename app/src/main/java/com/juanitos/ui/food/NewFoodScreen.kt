package com.juanitos.ui.food

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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

    Scaffold(topBar = {
        JuanitOSTopAppBar(
            title = stringResource(NewFoodDestination.titleRes),
            canNavigateBack = true,
            navigateUp = onNavigateUp,
        )
    }) { innerPadding ->
        FormColumn(innerPadding) {
            IngredientSearch(
                query = query.value,
                expanded = viewModel.searchExpanded,
                onQueryChange = { viewModel.onQueryChange(it) },
                onExpandedChange = { viewModel.onExpandedChange(it) },
                onSearch = { viewModel.onSearch(it) },
                ingredientSearch = ingredients.value,
            )
            if (viewModel.newIngredientOpen) {
                NewIngredientDialog(
                    onDismissRequest = { viewModel.onNewIngredientClose() },
                    onSave = { viewModel.onNewIngredientSave() },
                    newIngredientUiState = viewModel.newIngredientUiState,
                    onNewIngredientChange = { name, kcal, protein ->
                        viewModel.onNewIngredientChange(name, kcal, protein)
                    }
                )
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
    Box(modifier = Modifier.fillMaxWidth()) {
        SearchBar(
            inputField = {
                SearchBarDefaults.InputField(
                    query = query,
                    onQueryChange = onQueryChange,
                    onSearch = onSearch,
                    expanded = expanded,
                    onExpandedChange = onExpandedChange,
                    placeholder = { Text(stringResource(R.string.search_ingredient)) },
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
            Column(modifier = Modifier.padding(dimensionResource(R.dimen.padding_medium)), verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))) {
                OutlinedTextField(
                    value =  newIngredientUiState.name,
                    onValueChange = { onNewIngredientChange(it, newIngredientUiState.kcal, newIngredientUiState.protein) },
                    label = { Text(stringResource(R.string.ingredient_name)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value =  newIngredientUiState.kcal,
                    onValueChange = { onNewIngredientChange(newIngredientUiState.name, it, newIngredientUiState.protein) },
                    label = { Text(stringResource(R.string.ingredient_calories)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value =  newIngredientUiState.protein,
                    onValueChange = { onNewIngredientChange(newIngredientUiState.name, newIngredientUiState.kcal, it) },
                    label = { Text(stringResource(R.string.ingredient_protein)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Button(
                    onClick = {
                        onSave(newIngredientUiState.name)
                        onDismissRequest()
                    },
                    modifier = Modifier.fillMaxWidth()
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