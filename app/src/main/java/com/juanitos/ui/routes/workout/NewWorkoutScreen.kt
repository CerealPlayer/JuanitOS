package com.juanitos.ui.routes.workout

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.juanitos.R
import com.juanitos.ui.AppViewModelProvider
import com.juanitos.ui.commons.DeleteConfirmationDialog
import com.juanitos.ui.navigation.JuanitOSTopAppBar
import com.juanitos.ui.navigation.NavigationDestination
import com.juanitos.ui.navigation.Routes
import com.juanitos.ui.routes.workout.components.WorkoutQuickAddPanel
import kotlinx.coroutines.launch

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
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
            ) {
                items(uiState.exerciseGroups, key = { it.id }) { group ->
                    ExerciseCard(
                        group = group,
                        onDeleteExercise = { viewModel.deleteExercise(group.id) },
                        onDeleteSet = { setId -> viewModel.deleteSet(group.id, setId) }
                    )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExerciseCard(
    group: ExerciseGroup,
    onDeleteExercise: () -> Unit,
    onDeleteSet: (Int) -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState()
    val showDeleteExerciseDialog = remember { mutableStateOf(false) }
    val pendingSetDeletion = remember { mutableStateOf<Int?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
            showDeleteExerciseDialog.value = true
        }
    }

    if (showDeleteExerciseDialog.value) {
        DeleteConfirmationDialog(
            title = stringResource(R.string.confirm_delete_exercise),
            onConfirm = onDeleteExercise,
            onDismiss = {
                showDeleteExerciseDialog.value = false
                coroutineScope.launch { dismissState.reset() }
            }
        )
    }

    val setIdToDelete = pendingSetDeletion.value
    if (setIdToDelete != null) {
        DeleteConfirmationDialog(
            title = stringResource(R.string.delete),
            onConfirm = { onDeleteSet(setIdToDelete) },
            onDismiss = { pendingSetDeletion.value = null }
        )
    }

    SwipeToDismissBox(
        state = dismissState,
        modifier = Modifier.fillMaxWidth(),
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Red),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    painter = painterResource(R.drawable.delete),
                    contentDescription = stringResource(R.string.delete),
                    tint = Color.White,
                    modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.padding_medium))
                )
            }
        }
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(dimensionResource(R.dimen.padding_small))) {
                Text(
                    text = group.exercise.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = stringResource(
                        if (group.exercise.type == NewWorkoutViewModel.TYPE_REPS) R.string.exercise_type_reps
                        else R.string.exercise_type_duration
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (group.sets.isNotEmpty()) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    group.sets.forEach { set ->
                        SetRow(
                            set = set,
                            exerciseType = group.exercise.type,
                            onDeleteClick = { pendingSetDeletion.value = set.id }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SetRow(
    set: SetDisplay,
    exerciseType: String,
    onDeleteClick: () -> Unit,
) {
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
        IconButton(onClick = onDeleteClick, modifier = Modifier.size(24.dp)) {
            Icon(
                painterResource(R.drawable.close),
                contentDescription = stringResource(R.string.delete_set)
            )
        }
    }
}
