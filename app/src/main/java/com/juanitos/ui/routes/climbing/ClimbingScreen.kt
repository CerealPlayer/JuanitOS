package com.juanitos.ui.routes.climbing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.juanitos.R
import com.juanitos.lib.formatDbDatetimeToShortDate
import com.juanitos.lib.formatTimeToShort
import com.juanitos.ui.AppViewModelProvider
import com.juanitos.ui.navigation.JuanitOSTopAppBar
import com.juanitos.ui.navigation.NavigationDestination
import com.juanitos.ui.navigation.Routes

object ClimbingDestination : NavigationDestination {
    override val route = Routes.Climbing
    override val titleRes = R.string.climbing
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClimbingScreen(
    onNavigateUp: () -> Unit,
    onNewWorkout: () -> Unit,
    onWorkoutClick: (Int) -> Unit,
    viewModel: ClimbingViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val uiState = viewModel.uiState.collectAsState().value

    Scaffold(
        topBar = {
            JuanitOSTopAppBar(
                title = stringResource(ClimbingDestination.titleRes),
                canNavigateBack = true,
                navigateUp = onNavigateUp,
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNewWorkout) {
                Icon(
                    painter = painterResource(R.drawable.add),
                    contentDescription = stringResource(R.string.new_climbing_workout)
                )
            }
        },
    ) { innerPadding ->
        val workouts = uiState.workouts
        if (workouts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = stringResource(R.string.no_climbing_workouts))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = innerPadding.calculateTopPadding(),
                        bottom = innerPadding.calculateBottomPadding(),
                        start = dimensionResource(R.dimen.padding_small),
                        end = dimensionResource(R.dimen.padding_small),
                    ),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small)),
            ) {
                items(workouts, key = { it.id }) { workout ->
                    ClimbingWorkoutCard(
                        workout = workout,
                        onClick = { onWorkoutClick(workout.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ClimbingWorkoutCard(
    workout: ClimbingWorkoutCardUiState,
    onClick: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Column(modifier = Modifier.padding(dimensionResource(R.dimen.padding_small))) {
            Text(
                text = formatDbDatetimeToShortDate(workout.date).ifBlank { workout.date },
                style = MaterialTheme.typography.titleSmall,
            )
            val startTime = formatTimeToShort(workout.startTime)
            val endTime = formatTimeToShort(workout.endTime)
            if (startTime.isNotBlank() || endTime.isNotBlank()) {
                val timeText = when {
                    startTime.isNotBlank() && endTime.isNotBlank() -> "$startTime - $endTime"
                    startTime.isNotBlank() -> startTime
                    else -> endTime
                }
                Text(
                    text = timeText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            Text(
                text = stringResource(R.string.climbing_boulders_done, workout.bouldersDoneCount),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}
