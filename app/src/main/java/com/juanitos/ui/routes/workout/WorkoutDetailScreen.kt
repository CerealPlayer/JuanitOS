package com.juanitos.ui.routes.workout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.juanitos.R
import com.juanitos.data.workout.entities.WorkoutSet
import com.juanitos.data.workout.entities.relations.WorkoutExerciseWithSets
import com.juanitos.lib.formatDbDatetimeToShortDate
import com.juanitos.lib.formatTimeToShort
import com.juanitos.ui.AppViewModelProvider
import com.juanitos.ui.commons.DeleteConfirmationDialog
import com.juanitos.ui.icons.MoreVert
import com.juanitos.ui.navigation.JuanitOSTopAppBar
import com.juanitos.ui.navigation.NavigationDestination
import com.juanitos.ui.navigation.Routes

object WorkoutDetailDestination : NavigationDestination {
    override val route = Routes.WorkoutDetail
    override val titleRes = R.string.workout
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutDetailScreen(
    onNavigateUp: () -> Unit,
    viewModel: WorkoutDetailViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val uiState = viewModel.uiState.collectAsState()
    val workoutWithExercises = uiState.value.workoutWithExercises
    val showMenu = remember { mutableStateOf(false) }
    val showDeleteConfirmation = remember { mutableStateOf(false) }

    if (showDeleteConfirmation.value) {
        DeleteConfirmationDialog(
            title = stringResource(R.string.confirm_delete_workout),
            onConfirm = {
                onNavigateUp()
                workoutWithExercises?.workout?.let { viewModel.deleteWorkout(it) }
            },
            onDismiss = { showDeleteConfirmation.value = false },
        )
    }

    Scaffold(
        topBar = {
            JuanitOSTopAppBar(
                title = stringResource(WorkoutDetailDestination.titleRes),
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
        }
    ) { innerPadding ->
        if (workoutWithExercises == null) return@Scaffold

        val workout = workoutWithExercises.workout
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = innerPadding.calculateTopPadding(),
                    bottom = innerPadding.calculateBottomPadding(),
                    start = dimensionResource(R.dimen.padding_small),
                    end = dimensionResource(R.dimen.padding_small),
                ),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small)),
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(dimensionResource(R.dimen.padding_small))) {
                        Text(
                            text = formatDbDatetimeToShortDate(workout.date).ifBlank { workout.date },
                            style = MaterialTheme.typography.titleMedium,
                        )
                        val startTime = formatTimeToShort(workout.startTime)
                        val endTime = formatTimeToShort(workout.endTime)
                        if (startTime.isNotBlank() || endTime.isNotBlank()) {
                            val timeText = when {
                                startTime.isNotBlank() && endTime.isNotBlank() -> "$startTime – $endTime"
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
                        if (!workout.notes.isNullOrBlank()) {
                            Text(
                                text = workout.notes,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                    }
                }
            }

            items(
                workoutWithExercises.exercises.sortedBy { it.workoutExercise.position },
                key = { it.workoutExercise.id },
            ) { exerciseWithSets ->
                WorkoutDetailExerciseCard(exerciseWithSets = exerciseWithSets)
            }
        }
    }
}

@Composable
private fun WorkoutDetailExerciseCard(exerciseWithSets: WorkoutExerciseWithSets) {
    val exerciseType = exerciseWithSets.exerciseDefinition.type
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(dimensionResource(R.dimen.padding_small))) {
            Text(
                text = exerciseWithSets.exerciseDefinition.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = stringResource(
                    if (exerciseType == NewWorkoutViewModel.TYPE_REPS) R.string.exercise_type_reps
                    else R.string.exercise_type_duration
                ),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            val sortedSets = exerciseWithSets.sets.sortedBy { it.position }
            if (sortedSets.isNotEmpty()) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                sortedSets.forEachIndexed { index, set ->
                    WorkoutDetailSetRow(
                        setNumber = index + 1,
                        set = set,
                        exerciseType = exerciseType,
                    )
                }
            }
        }
    }
}

@Composable
private fun WorkoutDetailSetRow(setNumber: Int, set: WorkoutSet, exerciseType: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.set_label, setNumber),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(48.dp),
        )
        Text(
            text = if (set.weightKg != null && set.weightKg > 0)
                stringResource(R.string.kg_format, set.weightKg)
            else
                stringResource(R.string.bodyweight),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = if (exerciseType == NewWorkoutViewModel.TYPE_REPS)
                stringResource(R.string.reps_format, set.reps ?: 0)
            else
                stringResource(R.string.duration_format, set.durationSeconds ?: 0),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
        )
    }
}
