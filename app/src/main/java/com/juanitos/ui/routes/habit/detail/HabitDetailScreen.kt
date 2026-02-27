package com.juanitos.ui.routes.habit.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.juanitos.R
import com.juanitos.lib.formatDbDatetimeToShortDate
import com.juanitos.ui.AppViewModelProvider
import com.juanitos.ui.commons.DeleteConfirmationDialog
import com.juanitos.ui.icons.MoreVert
import com.juanitos.ui.navigation.JuanitOSTopAppBar
import com.juanitos.ui.navigation.NavigationDestination
import com.juanitos.ui.navigation.Routes

object HabitDetailDestination : NavigationDestination {
    override val route = Routes.HabitDetail
    override val titleRes = R.string.habit_detail

    fun createRoute(habitId: Int): String = "habit_detail/$habitId"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitDetailScreen(
    onNavigateUp: () -> Unit,
    viewModel: HabitDetailViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val uiState = viewModel.uiState.collectAsState().value
    val showMenu = remember { mutableStateOf(false) }
    val showDeleteConfirmation = remember { mutableStateOf(false) }
    val habit = uiState.habit

    if (showDeleteConfirmation.value && habit != null) {
        DeleteConfirmationDialog(
            title = stringResource(R.string.confirm_delete_habit),
            onConfirm = {
                onNavigateUp()
                viewModel.deleteHabit(habit)
            },
            onDismiss = { showDeleteConfirmation.value = false },
        )
    }

    Scaffold(
        topBar = {
            JuanitOSTopAppBar(
                title = stringResource(HabitDetailDestination.titleRes),
                canNavigateBack = true,
                navigateUp = onNavigateUp,
                actions = {
                    Box {
                        IconButton(onClick = { showMenu.value = true }) {
                            MoreVert()
                        }
                        DropdownMenu(
                            expanded = showMenu.value,
                            onDismissRequest = { showMenu.value = false },
                        ) {
                            if (habit?.completedAt == null) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.mark_habit_as_completed)) },
                                    onClick = {
                                        showMenu.value = false
                                        viewModel.markHabitAsCompleted()
                                    },
                                )
                            } else {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.unmark_habit_as_completed)) },
                                    onClick = {
                                        showMenu.value = false
                                        viewModel.unmarkHabitAsCompleted()
                                    },
                                )
                            }
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.delete)) },
                                onClick = {
                                    showMenu.value = false
                                    showDeleteConfirmation.value = true
                                },
                            )
                        }
                    }
                },
            )
        },
        bottomBar = {
            BottomAppBar(
                actions = {},
                floatingActionButton = {
                    FloatingActionButton(onClick = { viewModel.markCompletedToday() }) {
                        Icon(
                            painter = painterResource(R.drawable.check),
                            contentDescription = stringResource(R.string.mark_habit_complete_today),
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        if (habit == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                Text(
                    text = stringResource(R.string.habit_not_found),
                    modifier = Modifier.padding(dimensionResource(R.dimen.padding_small)),
                )
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(dimensionResource(R.dimen.padding_small)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small)),
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(dimensionResource(R.dimen.padding_small))) {
                    val createdAt = formatDbDatetimeToShortDate(habit.createdAt)
                        .ifBlank { habit.createdAt.orEmpty() }
                        .ifBlank { stringResource(R.string.habit_unknown_date) }
                    val completedAt = formatDbDatetimeToShortDate(habit.completedAt)
                        .ifBlank { habit.completedAt.orEmpty() }
                    Text(
                        text = habit.name,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = if (completedAt.isNotBlank()) {
                            stringResource(R.string.habit_lifecycle_from_to, createdAt, completedAt)
                        } else {
                            stringResource(R.string.habit_lifecycle_from, createdAt)
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (!habit.description.isNullOrBlank()) {
                        Text(
                            text = habit.description,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }

            Text(
                text = stringResource(R.string.habit_activity_this_month),
                style = MaterialTheme.typography.titleSmall,
            )

            HabitActivityGraph(weekColumns = uiState.weekColumns)

            if (uiState.weekColumns.flatten().none { it.isCompleted }) {
                Text(
                    text = stringResource(R.string.habit_no_activity_this_month),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun HabitActivityGraph(
    weekColumns: List<List<HabitActivityCell>>,
    modifier: Modifier = Modifier,
) {
    val dayLabels = listOf("M", "T", "W", "T", "F", "S", "S")
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val cellSize = 14.dp
        val cellSpacing = 4.dp
        val labelsWidth = 14.dp
        val labelSpacing = 8.dp
        val availableGridWidth = (maxWidth - labelsWidth - labelSpacing).coerceAtLeast(0.dp)
        val maxWeeks = ((availableGridWidth + cellSpacing) / (cellSize + cellSpacing))
            .toInt()
            .coerceAtLeast(MIN_WEEKS_TO_SHOW)
        val visibleWeekColumns = weekColumns.takeLast(maxWeeks.coerceAtMost(weekColumns.size))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(top = 2.dp),
            ) {
                dayLabels.forEach { label ->
                    Box(modifier = Modifier.size(14.dp)) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                visibleWeekColumns.forEach { weekColumn ->
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        weekColumn.forEach { cell ->
                            val color = when {
                                cell.isFutureDate -> MaterialTheme.colorScheme.surfaceVariant.copy(
                                    alpha = 0.35f
                                )

                                cell.isCompleted -> Color(0xFF2DA44E)
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .background(color, RoundedCornerShape(2.dp))
                            )
                        }
                    }
                }
            }
        }
    }
}

private const val MIN_WEEKS_TO_SHOW = 8
