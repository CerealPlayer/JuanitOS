package com.juanitos.ui.food

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.juanitos.R
import com.juanitos.ui.AppViewModelProvider
import com.juanitos.ui.commons.FormColumn
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
    val uiState = viewModel.uiState
    val ingredients = uiState.ingredients.collectAsState()

    val newIngString = stringResource(R.string.new_ingredient)

    Scaffold(
        topBar = {
            JuanitOSTopAppBar(
                title = stringResource(NewBatchFoodDestination.titleRes),
                canNavigateBack = true,
                navigateUp = onNavigateUp,
            )
        },
    ) { innerPadding ->
        FormColumn(innerPadding) {
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
                items = listOf(newIngString) + ingredients.value.map { it.name },
                onItemSelect = { viewModel.selectIngredient(it) })
            if (uiState.ingredientQtDialogOpen && uiState.selectedIngredient != null) {
                IngredientQtDialog(
                    name = uiState.selectedIngredient.name,
                    qt = uiState.qtQuery,
                    onQtChange = { viewModel.updateQtQuery(it) },
                    onDismissRequest = { viewModel.dismissQtDialog() },
                    onSave = { viewModel.saveIngredientEntry() })
            }
            uiState.ingredientEntries.forEach {
                IngredientEntryCard(it)
            }
        }
    }
}

@Composable
fun IngredientQtDialog(
    onDismiss: () -> Unit,
    name: String,
    qt: String,
    onQtChange: (String) -> Unit,
    onSave: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card {
            Row {
                Text(name, style = MaterialTheme.typography.titleMedium)
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
                onClick = onSave
            ) {
                Text(stringResource(R.string.save))
            }
        }
    }
}
