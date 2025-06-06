package com.juanitos.ui.routes.food.batch.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.juanitos.ui.commons.SearchResult
import com.juanitos.ui.commons.food.IngredientEntryCard
import com.juanitos.ui.navigation.JuanitOSTopAppBar
import com.juanitos.ui.navigation.NavigationDestination
import com.juanitos.ui.navigation.Routes

object EditBatchFoodDestination : NavigationDestination {
    override val route = Routes.EditBatchFood
    override val titleRes = R.string.edit_batch_food
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBatchFoodScreen(
    onNavigateUp: () -> Unit,
    onNewIngredient: () -> Unit,
    viewModel: EditBatchFoodViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState = viewModel.uiState.collectAsState().value
    val ingredients = uiState.ingredients

    val newIngString = stringResource(R.string.new_ingredient)
    val newIngSearchResult = SearchResult(id = newIngString, label = {
        Text(newIngString, color = MaterialTheme.colorScheme.primary)
    }, onItemSelect = {
        viewModel.updateSearchQuery("")
        onNewIngredient()
    })

    val searchResults = listOf(newIngSearchResult) + ingredients.map {
        SearchResult(id = it.name, label = {
            Text(it.name)
        }, onItemSelect = { viewModel.selectIngredient(it.name) })
    }

    Scaffold(topBar = {
        JuanitOSTopAppBar(
            title = stringResource(EditBatchFoodDestination.titleRes),
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
            Search(query = uiState.searchQuery, expanded = uiState.searchExpanded, onQueryChange = {
                viewModel.updateSearchQuery(it)
            }, onExpandedChange = { viewModel.updateSearchExpanded(it) }, onSearch = {
                viewModel.updateSearchQuery(it)
            }, searchResults = searchResults
            )
            if (uiState.ingredientQtDialogOpen && uiState.selectedIngredient != null) {
                IngredientQtDialog(
                    name = uiState.selectedIngredient.name,
                    qt = uiState.qtQuery,
                    onQtChange = { viewModel.updateQtQuery(it) },
                    onDismissRequest = { viewModel.dismissQtDialog() },
                    onSave = { viewModel.saveIngredientEntry() })
            }
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(
                    dimensionResource(R.dimen.padding_small)
                ),
            ) {
                items(uiState.ingredientEntries) { entry ->
                    val dismissState = rememberSwipeToDismissBoxState()

                    LaunchedEffect(dismissState.currentValue) {
                        if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
                            viewModel.deleteIngredientEntry(entry)
                            dismissState.reset()
                        }
                    }
                    SwipeToDismissBox(
                        state = dismissState,
                        enableDismissFromStartToEnd = false,
                        backgroundContent = {
                            DismissBackground(progress = dismissState.progress)
                        },

                        ) {
                        IngredientEntryCard(entry)
                    }
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

@Composable
private fun DismissBackground(
    progress: Float = 0f,
) {
    val colorIntensity = ((1 - progress) * 255).toInt().coerceIn(0, 255)
    val iconColor = Color(255, colorIntensity, colorIntensity)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(dimensionResource(R.dimen.padding_small)),
        contentAlignment = Alignment.CenterEnd
    ) {
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "Delete",
            tint = iconColor
        )
    }
}