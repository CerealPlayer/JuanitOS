package com.juanitos.ui.food

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.juanitos.R
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
    Log.d("NewFoodScreen", viewModel.newIngredientOpen.toString())
    val uiState = viewModel.uiState.collectAsState()
    Scaffold(topBar = {
        JuanitOSTopAppBar(
            title = stringResource(NewFoodDestination.titleRes),
            canNavigateBack = true,
            navigateUp = onNavigateUp,
        )
    }) { innerPadding ->
        FormColumn(innerPadding) {
            IngredientSearch(
                query = viewModel.ingredientQuery,
                expanded = viewModel.searchExpanded,
                onQueryChange = { viewModel.onQueryChange(it) },
                onExpandedChange = { viewModel.onExpandedChange(it) },
                onSearch = { viewModel.onSearch(it) },
                uiState = uiState.value
            )
            if (viewModel.newIngredientOpen) {
                NewIngredientDialog(
                    onDismissRequest = { viewModel.onIngredientOpenChange(false) },
                    onSave = { }
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
    uiState: NewFoodUiState
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
                uiState.ingredientSearch.forEach { ingredient ->
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
    onSave: (String) -> Unit,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
    ) {
        Card {
            Text(text = stringResource(R.string.add_ingredient), style = MaterialTheme.typography.titleLarge)
        }
    }
}