package com.juanitos.ui.food

import android.util.Log
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.juanitos.R
import com.juanitos.ui.AppViewModelProvider
import com.juanitos.ui.commons.FormColumn
import com.juanitos.ui.commons.Search
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
    viewModel: NewBatchFoodViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState = viewModel.uiState
    val ingredients = uiState.ingredients.collectAsState()

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
                onQueryChange = {viewModel.updateSearchQuery(it)},
                onExpandedChange = {viewModel.updateSearchExpanded(it)},
                onSearch = {viewModel.updateSearchQuery(it)},
                items = ingredients.value.map { it.name },
            )
        }
    }
}