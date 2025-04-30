package com.juanitos.ui.food

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.juanitos.R
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
) {
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
                query = "",
                expanded = false,
                onQueryChange = {},
                onExpandedChange = {},
                onSearch = {},
                items = emptyList(),
            )
        }
    }
}