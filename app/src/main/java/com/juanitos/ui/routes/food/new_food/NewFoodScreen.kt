package com.juanitos.ui.routes.food.new_food

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.juanitos.R
import com.juanitos.ui.AppViewModelProvider
import com.juanitos.ui.commons.FormColumn
import com.juanitos.ui.commons.IngredientQtDialog
import com.juanitos.ui.commons.Search
import com.juanitos.ui.commons.SearchResult
import com.juanitos.ui.commons.food.BatchFoodEntryCard
import com.juanitos.ui.commons.food.IngredientEntryCard
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
    viewModel: NewFoodViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState = viewModel.uiState.collectAsState().value
    val ingredients = uiState.ingredients
    val batchFoods = uiState.batchFoods

    val newIngString = stringResource(R.string.new_ingredient)
    val newIngSearchResult = SearchResult(id = newIngString, label = {
        Text(newIngString, color = MaterialTheme.colorScheme.primary)
    }, onItemSelect = { onNewIngredient() })

    val searchResults = listOf(newIngSearchResult) + ingredients.map {
        SearchResult(
            id = it.name,
            label = {
                Text(it.name)
            },
            onItemSelect = { viewModel.selectIngredient(it.name) },
            tag = { Text(stringResource(R.string.food_tag)) })
    } + batchFoods.map {
        SearchResult(
            id = it.name,
            label = {
                Text(it.name)
            },
            onItemSelect = { viewModel.selectBatchFood(it.name) },
            tag = { Text(stringResource(R.string.batch_tag)) })
    }

    Scaffold(topBar = {
        JuanitOSTopAppBar(
            title = stringResource(NewFoodDestination.titleRes),
            canNavigateBack = true,
            navigateUp = onNavigateUp,
        )
    }, bottomBar = {
        Button(
            onClick = { viewModel.updateSaveDialogOpen(true) },
            enabled = uiState.ingredientEntries.isNotEmpty() || uiState.batchFoodEntries.isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.padding_medium))
        ) {
            Text(stringResource(R.string.save))
        }
    }) { innerPadding ->
        FormColumn(
            innerPadding
        ) {
            Search(query = uiState.searchQuery, expanded = uiState.searchExpanded, onQueryChange = {
                viewModel.updateSearchQuery(it)
            }, onExpandedChange = { viewModel.updateSearchExpanded(it) }, onSearch = {
                viewModel.updateSearchQuery(it)
            }, searchResults = searchResults)
            if (uiState.ingredientQtDialogOpen && uiState.selectedIngredient != null) {
                IngredientQtDialog(
                    name = uiState.selectedIngredient.name,
                    qt = uiState.qtQuery,
                    onQtChange = { viewModel.updateQtQuery(it) },
                    onDismissRequest = { viewModel.dismissQtDialog() },
                    onSave = { viewModel.saveIngredientEntry() })
            }
            if (uiState.batchFoodQtDialogOpen && uiState.selectedBatchFood != null) {
                BatchFoodQtDialog(
                    name = uiState.selectedBatchFood.name,
                    qt = uiState.qtQuery,
                    onQtChange = { viewModel.updateQtQuery(it) },
                    onDismiss = { viewModel.dismissBatchFoodQtDialog() },
                    onSave = { viewModel.saveBatchFoodEntry() },
                    totalGrams = uiState.selectedBatchFood.totalGrams
                )
            }
            LazyColumn {
                items(uiState.ingredientEntries) { entry ->
                    IngredientEntryCard(entry)
                }
                items(uiState.batchFoodEntries) { entry ->
                    BatchFoodEntryCard(entry)
                }
            }
            if (uiState.saveDialogOpen) {
                SaveDialog(
                    onDismiss = { viewModel.updateSaveDialogOpen(false) },
                    name = uiState.batchFoodNameQuery,
                    onNameChange = { viewModel.updateFoodName(it) },
                    onSave = { viewModel.saveFood(onNavigateUp) })
            }
        }
    }
}

@Composable
fun BatchFoodQtDialog(
    name: String,
    qt: String,
    onQtChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    totalGrams: Int
) {
    val qtInt = qt.toIntOrNull() ?: 0
    IngredientQtDialog(
        name = name,
        qt = qt,
        onQtChange = onQtChange,
        onDismissRequest = onDismiss,
        onSave = onSave,
        customMessage = {
            Text(
                stringResource(R.string.grams_left, totalGrams - qtInt),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
            )
        },
        isError = qtInt > totalGrams
    )
}

@Composable
fun SaveDialog(
    onDismiss: () -> Unit, name: String, onNameChange: (String) -> Unit, onSave: () -> Unit
) {
    Dialog(onDismiss) {
        Card {
            Column(
                modifier = Modifier.padding(dimensionResource(R.dimen.padding_medium)),
                verticalArrangement = Arrangement.spacedBy(
                    dimensionResource(R.dimen.padding_small)
                )
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text(stringResource(R.string.food_name)) },
                    singleLine = true
                )
                Button(onClick = onSave, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.save))
                }
            }
        }
    }
}