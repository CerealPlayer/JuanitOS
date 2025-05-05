package com.juanitos.ui.routes.food.batch

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import com.juanitos.ui.AppViewModelProvider
import com.juanitos.ui.commons.FormColumn
import com.juanitos.ui.commons.IngredientQtDialog
import com.juanitos.ui.commons.Search
import com.juanitos.ui.commons.food.IngredientEntryCard
import com.juanitos.ui.navigation.JuanitOSTopAppBar
import com.juanitos.ui.navigation.NavigationDestination
import com.juanitos.ui.navigation.Routes

object NewBatchFoodDestination : NavigationDestination {
    override val route = Routes.NewBatchFood
    override val titleRes = R.string.new_batch_food
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewBatchFoodScreen(
    onNavigateUp: () -> Unit,
    onNewIngredient: () -> Unit,
    viewModel: NewBatchFoodViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState = viewModel.uiState.collectAsState().value
    val ingredients = uiState.ingredients

    val newIngString = stringResource(R.string.new_ingredient)

    Scaffold(topBar = {
        JuanitOSTopAppBar(
            title = stringResource(NewBatchFoodDestination.titleRes),
            canNavigateBack = true,
            navigateUp = onNavigateUp,
        )
    }, bottomBar = {
        Button(
            onClick = { viewModel.updateSaveDialogOpen(true) },
            enabled = uiState.ingredientEntries.isNotEmpty(),
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
            Search(
                query = uiState.searchQuery,
                expanded = uiState.searchExpanded,
                onQueryChange = {
                    if (it == newIngString) {
                        onNewIngredient()
                    } else {
                        viewModel.updateSearchQuery(it)
                    }
                },
                onExpandedChange = { viewModel.updateSearchExpanded(it) },
                onSearch = {
                    if (it == newIngString) {
                        onNewIngredient()
                    } else {
                        viewModel.updateSearchQuery(it)
                    }
                },
                searchResults = listOf(newIngString) + ingredients.map { it.name },
                onItemSelect = { viewModel.selectIngredient(it) })
            if (uiState.ingredientQtDialogOpen && uiState.selectedIngredient != null) {
                IngredientQtDialog(
                    name = uiState.selectedIngredient.name,
                    qt = uiState.qtQuery,
                    onQtChange = { viewModel.updateQtQuery(it) },
                    onDismissRequest = { viewModel.dismissQtDialog() },
                    onSave = { viewModel.saveIngredientEntry() })
            }
            LazyColumn {
                items(uiState.ingredientEntries) { entry ->
                    IngredientEntryCard(entry)
                }
            }
            if (uiState.saveDialogOpen) {
                SaveDialog(
                    onDismiss = { viewModel.updateSaveDialogOpen(false) },
                    name = uiState.batchFoodNameQuery,
                    totalGrams = uiState.batchFoodTotalGramsQuery,
                    onNameChange = { viewModel.updateBatchFoodName(it) },
                    onTotalGramsChange = { viewModel.updateBatchFoodTotalGrams(it) },
                    onSave = { viewModel.saveBatchFood(onNavigateUp) })
            }
        }
    }
}

@Composable
fun SaveDialog(
    onDismiss: () -> Unit,
    name: String,
    totalGrams: String,
    onNameChange: (String) -> Unit,
    onTotalGramsChange: (String) -> Unit,
    onSave: () -> Unit
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
                OutlinedTextField(
                    value = totalGrams,
                    onValueChange = onTotalGramsChange,
                    label = { Text(stringResource(R.string.total_grams)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
                Button(onClick = onSave, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.save))
                }
            }
        }
    }
}