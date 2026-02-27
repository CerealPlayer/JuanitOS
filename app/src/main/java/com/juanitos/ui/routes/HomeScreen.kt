package com.juanitos.ui.routes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.juanitos.R
import com.juanitos.ui.AppViewModelProvider
import com.juanitos.ui.commons.MoneySummaryChart
import com.juanitos.ui.navigation.JuanitOSTopAppBar
import com.juanitos.ui.navigation.NavigationDestination
import com.juanitos.ui.navigation.Routes
import com.juanitos.ui.routes.habit.HabitsDestination
import com.juanitos.ui.routes.money.MoneyDestination
import com.juanitos.ui.routes.workout.WorkoutDestination

object HomeDestination : NavigationDestination {
    override val route = Routes.Home
    override val titleRes = R.string.app_name
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateTo: (Routes) -> Unit,
    onNavigateToHabitDetail: (Int) -> Unit,
    viewModel: HomeViewModel = viewModel(
        factory = AppViewModelProvider.Factory
    )
) {
    val uiState = viewModel.uiState.collectAsState()
    val summary = uiState.value.summary
    val habitsPreview = uiState.value.habitsPreview

    Scaffold(topBar = {
        JuanitOSTopAppBar(
            title = stringResource(HomeDestination.titleRes),
            canNavigateBack = false,
        )
    }, bottomBar = {
        BottomAppBar(actions = {
            IconButton(onClick = { onNavigateTo(MoneyDestination.route) }) {
                Icon(
                    painter = painterResource(R.drawable.subapp_money),
                    contentDescription = stringResource(R.string.money)
                )
            }
            IconButton(onClick = { onNavigateTo(WorkoutDestination.route) }) {
                Icon(
                    painter = painterResource(R.drawable.subapp_workout),
                    contentDescription = stringResource(R.string.workout)
                )
            }
            IconButton(onClick = { onNavigateTo(HabitsDestination.route) }) {
                Icon(
                    painter = painterResource(R.drawable.subapp_habits),
                    contentDescription = stringResource(R.string.habits)
                )
            }
        })
    }) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxHeight()
                .padding(
                    start = dimensionResource(R.dimen.padding_small),
                    end = dimensionResource(R.dimen.padding_small)
                ),
        ) {
            // Summary chart
            MoneySummaryChart(summary = summary)

            if (habitsPreview.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = dimensionResource(R.dimen.padding_small)),
                    horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small)),
                ) {
                    habitsPreview.take(3).forEach { habit ->
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNavigateToHabitDetail(habit.habitId) },
                        ) {
                            Column(
                                modifier = Modifier.padding(dimensionResource(R.dimen.padding_small)),
                                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small)),
                            ) {
                                Text(
                                    text = habit.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    maxLines = 1,
                                )
                                MiniHabitActivityGraph(weekColumns = habit.weekColumns)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniHabitActivityGraph(
    weekColumns: List<List<com.juanitos.ui.routes.habit.detail.HabitActivityCell>>,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val cellSize = 11.dp
        val cellSpacing = 2.dp
        val maxColumns = ((maxWidth + cellSpacing) / (cellSize + cellSpacing))
            .toInt()
            .coerceAtLeast(1)
            .coerceAtMost(MAX_PREVIEW_COLUMNS)
        val recentCells = weekColumns
            .flatten()
            .takeLast(maxColumns * PREVIEW_ROWS)
        val visibleColumns = recentCells
            .chunked(PREVIEW_ROWS)
            .takeLast(maxColumns)

        Row(horizontalArrangement = Arrangement.spacedBy(cellSpacing)) {
            visibleColumns.forEach { columnCells ->
                Column(verticalArrangement = Arrangement.spacedBy(cellSpacing)) {
                    columnCells.forEach { cell ->
                        val color = when {
                            cell.isFutureDate -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                            cell.isCompleted -> Color(0xFF2DA44E)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                        Box(
                            modifier = Modifier
                                .size(cellSize)
                                .background(color, RoundedCornerShape(2.dp))
                        )
                    }
                }
            }
        }
    }
}

private const val PREVIEW_ROWS = 3
private const val MAX_PREVIEW_COLUMNS = 8
