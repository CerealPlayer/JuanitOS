package com.juanitos.ui.routes.workout

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.juanitos.R
import com.juanitos.data.workout.entities.ExerciseDefinition
import com.juanitos.ui.AppViewModelProvider
import com.juanitos.ui.navigation.JuanitOSTopAppBar
import com.juanitos.ui.navigation.NavigationDestination
import com.juanitos.ui.navigation.Routes

object NewWorkoutDestination : NavigationDestination {
    override val route = Routes.NewWorkout
    override val titleRes = R.string.new_workout
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewWorkoutScreen(
    onNavigateUp: () -> Unit,
    viewModel: NewWorkoutViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState = viewModel.uiState.collectAsState().value
    val listState = rememberLazyListState()
    val focusManager = LocalFocusManager.current

    // Intercept system back gesture / hardware back button
    BackHandler {
        focusManager.clearFocus()
        if (uiState.exerciseGroups.isEmpty()) {
            viewModel.discardWorkout(onNavigateUp)
        } else {
            viewModel.openDiscardDialog()
        }
    }

    // Scroll to end when a new set is added
    val totalSets = uiState.exerciseGroups.sumOf { it.sets.size }
    LaunchedEffect(totalSets) {
        if (uiState.exerciseGroups.isNotEmpty()) {
            val lastIndex = listState.layoutInfo.totalItemsCount.coerceAtLeast(1) - 1
            listState.animateScrollToItem(lastIndex)
        }
    }

    // Intercept top-bar back arrow
    val handleBack: () -> Unit = {
        focusManager.clearFocus()
        if (uiState.exerciseGroups.isEmpty()) {
            viewModel.discardWorkout(onNavigateUp)
        } else {
            viewModel.openDiscardDialog()
        }
    }

    Scaffold(
        // Opt out of automatic inset handling; the bottomBar manages its own insets
        contentWindowInsets = WindowInsets(0),
        topBar = {
            JuanitOSTopAppBar(
                title = stringResource(NewWorkoutDestination.titleRes),
                canNavigateBack = true,
                navigateUp = handleBack,
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
            QuickAddPanel(
                uiState = uiState,
                onSelectExercise = { viewModel.selectExercise(it) },
                onWeightChange = { viewModel.setWeightInput(it) },
                onRepsOrDurationChange = { viewModel.setRepsOrDurationInput(it) },
                onAddSet = {
                    focusManager.clearFocus()
                    viewModel.addSet()
                }
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

    // ── Save dialog ──────────────────────────────────────────────────────────
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
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
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

    // ── Discard dialog ───────────────────────────────────────────────────────
    if (uiState.showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.closeDiscardDialog() },
            title = { Text(stringResource(R.string.discard_workout)) },
            text = { Text(stringResource(R.string.discard_workout_message)) },
            confirmButton = {
                Button(
                    onClick = { viewModel.discardWorkout(onNavigateUp) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.discard))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.closeDiscardDialog() }) {
                    Text(stringResource(R.string.keep_editing))
                }
            }
        )
    }
}

@Composable
private fun QuickAddPanel(
    uiState: NewWorkoutUiState,
    onSelectExercise: (ExerciseDefinition) -> Unit,
    onWeightChange: (String) -> Unit,
    onRepsOrDurationChange: (String) -> Unit,
    onAddSet: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    Surface(tonalElevation = 8.dp, shadowElevation = 8.dp) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .navigationBarsPadding()
                .padding(
                    horizontal = dimensionResource(R.dimen.padding_small),
                    vertical = dimensionResource(R.dimen.padding_small)
                ),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
        ) {
            if (uiState.allExercises.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_exercises_defined),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                // Exercise selector chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
                ) {
                    uiState.allExercises.forEach { exercise ->
                        FilterChip(
                            selected = uiState.selectedExercise?.id == exercise.id,
                            onClick = { onSelectExercise(exercise) },
                            label = { Text(text = exercise.name) }
                        )
                    }
                }

                HorizontalDivider()

                val isReps = uiState.selectedExercise?.type == NewWorkoutViewModel.TYPE_REPS
                val inputLabel = if (isReps)
                    stringResource(R.string.exercise_type_reps)
                else
                    stringResource(R.string.duration_seconds)

                // Weight + reps/duration inputs + add button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = uiState.weightInput,
                        onValueChange = onWeightChange,
                        label = { Text(stringResource(R.string.weight_kg)) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Next) }
                        ),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = uiState.repsOrDurationInput,
                        onValueChange = onRepsOrDurationChange,
                        label = { Text(inputLabel) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { focusManager.clearFocus(); onAddSet() }
                        ),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = onAddSet,
                        enabled = !uiState.isSavingSet && uiState.selectedExercise != null,
                        modifier = Modifier.width(64.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(text = "+", style = MaterialTheme.typography.titleLarge)
                    }
                }

                if (uiState.errorMessage != null) {
                    Text(
                        text = uiState.errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
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
