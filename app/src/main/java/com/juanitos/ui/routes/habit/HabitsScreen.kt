package com.juanitos.ui.routes.habit

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.juanitos.R
import com.juanitos.ui.AppViewModelProvider
import com.juanitos.ui.navigation.JuanitOSTopAppBar
import com.juanitos.ui.navigation.NavigationDestination
import com.juanitos.ui.navigation.Routes

object HabitsDestination : NavigationDestination {
    override val route = Routes.Habits
    override val titleRes = R.string.habits
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitsScreen(
    onNavigateUp: () -> Unit,
    viewModel: HabitsViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val uiState = viewModel.uiState.collectAsState()
    val today = uiState.value.today

    Scaffold(
        topBar = {
            JuanitOSTopAppBar(
                title = stringResource(HabitsDestination.titleRes),
                canNavigateBack = true,
                navigateUp = onNavigateUp,
            )
        }
    ) { innerPadding ->
        if (uiState.value.habitsWithEntries.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(text = stringResource(R.string.no_habits))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(
                        horizontal = dimensionResource(R.dimen.padding_small),
                        vertical = dimensionResource(R.dimen.padding_small)
                    )
            ) {
                items(uiState.value.habitsWithEntries, key = { it.habit.id }) { habitWithEntries ->
                    val todayEntry = habitWithEntries.entries.find { it.date == today }
                    val isCompleted = todayEntry?.completed ?: false

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = dimensionResource(R.dimen.padding_small))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(dimensionResource(R.dimen.padding_small)),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Checkbox(
                                checked = isCompleted,
                                onCheckedChange = {
                                    viewModel.toggleEntry(
                                        habitId = habitWithEntries.habit.id,
                                        date = today,
                                        currentCompleted = isCompleted
                                    )
                                }
                            )
                            Column(modifier = Modifier.padding(start = dimensionResource(R.dimen.padding_small))) {
                                Text(
                                    text = habitWithEntries.habit.name,
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                if (!habitWithEntries.habit.description.isNullOrBlank()) {
                                    Text(
                                        text = habitWithEntries.habit.description,
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
