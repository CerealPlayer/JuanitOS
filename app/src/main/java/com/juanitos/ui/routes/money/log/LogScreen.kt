package com.juanitos.ui.routes.money.log

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.juanitos.R
import com.juanitos.data.money.entities.Category
import com.juanitos.ui.AppViewModelProvider
import com.juanitos.ui.navigation.JuanitOSTopAppBar
import com.juanitos.ui.navigation.NavigationDestination
import com.juanitos.ui.navigation.Routes
import com.juanitos.ui.routes.money.FixedSpendingCard
import com.juanitos.ui.routes.money.Movement
import com.juanitos.ui.routes.money.TransactionCard

object LogDestination : NavigationDestination {
    override val route = Routes.Log
    override val titleRes = R.string.log
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogScreen(
    onNavigateUp: () -> Unit,
    viewModel: LogViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState = viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            JuanitOSTopAppBar(
                title = stringResource(LogDestination.titleRes),
                canNavigateBack = true,
                navigateUp = onNavigateUp
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(
                    top = innerPadding.calculateTopPadding(),
                    bottom = innerPadding.calculateBottomPadding(),
                    start = dimensionResource(R.dimen.padding_small),
                    end = dimensionResource(R.dimen.padding_small)
                )
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
        ) {
            OutlinedTextField(
                value = uiState.value.searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                label = { Text(stringResource(R.string.search_log)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            CategoryFilter(
                categories = uiState.value.categories,
                selectedCategory = uiState.value.selectedCategory,
                onCategorySelected = { viewModel.setCategoryFilter(it) }
            )

            val movements = uiState.value.movements
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
            ) {
                items(movements, key = { movement ->
                    when (movement) {
                        is Movement.FixedSpendingMovement -> "fs-${movement.fixedSpending.fixedSpending.id}"
                        is Movement.TransactionMovement -> "t-${movement.transaction.transaction.id}"
                    }
                }) { movement ->
                    when (movement) {
                        is Movement.FixedSpendingMovement -> FixedSpendingCard(
                            fixedSpendingWithCategory = movement.fixedSpending
                        )

                        is Movement.TransactionMovement -> TransactionCard(
                            transactionWithCategory = movement.transaction,
                            onDelete = { viewModel.deleteTransaction(it.transaction) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryFilter(
    categories: List<Category>,
    selectedCategory: Category?,
    onCategorySelected: (Category?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
            modifier = Modifier.weight(1f)
        ) {
            OutlinedTextField(
                value = selectedCategory?.name ?: stringResource(R.string.all_categories),
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.category)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.all_categories)) },
                    onClick = {
                        onCategorySelected(null)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
                categories.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category.name) },
                        onClick = {
                            onCategorySelected(category)
                            expanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }
        if (selectedCategory != null) {
            TextButton(onClick = { onCategorySelected(null) }) {
                Text(stringResource(R.string.clear_filter))
            }
        }
    }
}
