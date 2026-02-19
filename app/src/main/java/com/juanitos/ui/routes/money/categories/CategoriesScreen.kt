package com.juanitos.ui.routes.money.categories

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.juanitos.R
import com.juanitos.data.money.entities.Category
import com.juanitos.ui.AppViewModelProvider
import com.juanitos.ui.icons.Add
import com.juanitos.ui.navigation.JuanitOSTopAppBar
import com.juanitos.ui.navigation.NavigationDestination
import com.juanitos.ui.navigation.Routes

object CategoriesDestination : NavigationDestination {
    override val route = Routes.Categories
    override val titleRes = R.string.categories
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    onNavigateUp: () -> Unit,
    onNewCategory: () -> Unit,
    viewModel: CategoriesViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val categories = viewModel.uiState.collectAsState().value.categories
    Scaffold(
        topBar = {
            JuanitOSTopAppBar(
                title = stringResource(CategoriesDestination.titleRes),
                canNavigateBack = true,
                navigateUp = onNavigateUp
            )
        },
        bottomBar = {
            BottomAppBar(
                actions = {},
                floatingActionButton = {
                    FloatingActionButton(onClick = onNewCategory) {
                        Add()
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(
                    top = innerPadding.calculateTopPadding(),
                    bottom = innerPadding.calculateBottomPadding(),
                    start = dimensionResource(R.dimen.padding_small),
                    end = dimensionResource(R.dimen.padding_small)
                ),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
        ) {
            items(categories) { category ->
                CategoryCard(category = category)
            }
        }
    }
}

@Composable
fun CategoryCard(category: Category) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(dimensionResource(R.dimen.padding_small))
        ) {
            Text(text = category.name)
            if (!category.description.isNullOrBlank()) {
                Text(
                    text = category.description,
                )
            }
        }
    }
}

