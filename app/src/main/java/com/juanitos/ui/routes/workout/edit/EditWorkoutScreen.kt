package com.juanitos.ui.routes.workout.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.juanitos.R
import com.juanitos.ui.AppViewModelProvider
import com.juanitos.ui.navigation.JuanitOSTopAppBar
import com.juanitos.ui.navigation.NavigationDestination
import com.juanitos.ui.navigation.Routes
import com.juanitos.ui.routes.workout.NewWorkoutViewModel
import com.juanitos.ui.routes.workout.SetDisplay
import com.juanitos.ui.routes.workout.components.WorkoutQuickAddPanel

object EditWorkoutDestination : NavigationDestination {
    override val route = Routes.EditWorkout
    override val titleRes = R.string.edit_workout

    fun createRoute(workoutId: Int): String = "edit_workout/$workoutId"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditWorkoutScreen(
    onNavigateUp: () -> Unit,
    viewModel: EditWorkoutViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val uiState = viewModel.uiState.collectAsState().value
    val listState = rememberLazyListState()

    val totalSets = uiState.exerciseGroups.sumOf { it.sets.size }
    LaunchedEffect(totalSets) {
        if (uiState.exerciseGroups.isNotEmpty()) {
            val lastIndex = listState.layoutInfo.totalItemsCount.coerceAtLeast(1) - 1
            listState.animateScrollToItem(lastIndex)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            JuanitOSTopAppBar(
                title = stringResource(EditWorkoutDestination.titleRes),
                canNavigateBack = true,
                navigateUp = onNavigateUp,
                actions = {
                    IconButton(
                        onClick = { viewModel.openSaveDialog() },
                        enabled = uiState.workoutId != null
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.check),
                            contentDescription = stringResource(R.string.save_workout)
                        )
                    }
                }
            )
        },
        bottomBar = {
            WorkoutQuickAddPanel(
                allExercises = uiState.allExercises,
                selectedExerciseId = uiState.selectedExercise?.id,
                selectedExerciseType = uiState.selectedExercise?.type,
                weightInput = uiState.weightInput,
                repsOrDurationInput = uiState.repsOrDurationInput,
                isSavingSet = uiState.isSavingSet,
                errorMessage = uiState.errorMessage,
                onSelectExercise = { viewModel.selectExercise(it) },
                onWeightChange = { viewModel.setWeightInput(it) },
                onRepsOrDurationChange = { viewModel.setRepsOrDurationInput(it) },
                onAddSet = { viewModel.addSet() }
            )
        }
    ) { innerPadding ->
        if (uiState.exerciseGroups.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (uiState.allExercises.isEmpty())
                        stringResource(R.string.no_exercises_defined)
                    else
                        stringResource(R.string.add_set),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = dimensionResource(R.dimen.padding_small)),
                contentPadding = PaddingValues(
                    top = innerPadding.calculateTopPadding() + dimensionResource(R.dimen.padding_small),
                    bottom = innerPadding.calculateBottomPadding() + dimensionResource(R.dimen.padding_small)
                ),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                uiState.exerciseGroups.forEach { group ->
                    item(key = "header-${group.exercise.id}") {
                        Text(
                            text = group.exercise.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(
                                top = dimensionResource(R.dimen.padding_small),
                                bottom = 2.dp
                            )
                        )
                    }
                    items(
                        group.sets,
                        key = { "set-${group.exercise.id}-${it.setNumber}" }
                    ) { set ->
                        SetRow(set = set, exerciseType = group.exercise.type)
                    }
                }
                item { Spacer(modifier = Modifier.height(4.dp)) }
            }
        }
    }

    if (uiState.showSaveDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.closeSaveDialog() },
            title = { Text(stringResource(R.string.save_workout)) },
            text = {
                OutlinedTextField(
                    value = uiState.notesInput,
                    onValueChange = { viewModel.setNotesInput(it) },
                    label = { Text(stringResource(R.string.workout_notes_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false,
                    maxLines = 4,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        imeAction = ImeAction.Done
                    )
                )
            },
            confirmButton = {
                Button(onClick = { viewModel.saveWorkout(onNavigateUp) }) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.closeSaveDialog() }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun SetRow(set: SetDisplay, exerciseType: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.set_label, set.setNumber),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(48.dp)
        )
        val weightText = if (set.weightKg != null && set.weightKg > 0)
            stringResource(R.string.kg_format, set.weightKg)
        else
            stringResource(R.string.bodyweight)
        Text(
            text = weightText,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        val performanceText = if (exerciseType == NewWorkoutViewModel.TYPE_REPS) {
            stringResource(R.string.reps_format, set.reps ?: 0)
        } else {
            stringResource(R.string.duration_format, set.durationSeconds ?: 0)
        }
        Text(
            text = performanceText,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}
