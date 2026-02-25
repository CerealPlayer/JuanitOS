package com.juanitos.ui.routes.workout.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.juanitos.R
import com.juanitos.data.workout.entities.ExerciseDefinition

@Composable
fun WorkoutQuickAddPanel(
    allExercises: List<ExerciseDefinition>,
    selectedExerciseId: Int?,
    selectedExerciseType: String?,
    weightInput: String,
    repsOrDurationInput: String,
    isSavingSet: Boolean,
    errorMessage: String?,
    onSelectExercise: (ExerciseDefinition) -> Unit,
    onWeightChange: (String) -> Unit,
    onRepsOrDurationChange: (String) -> Unit,
    onAddSet: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    var weightFieldValue by remember { mutableStateOf(TextFieldValue(weightInput)) }
    var repsOrDurationFieldValue by remember { mutableStateOf(TextFieldValue(repsOrDurationInput)) }
    var weightFieldFocused by remember { mutableStateOf(false) }
    var repsOrDurationFieldFocused by remember { mutableStateOf(false) }

    LaunchedEffect(weightInput) {
        if (weightInput != weightFieldValue.text) {
            weightFieldValue =
                TextFieldValue(weightInput, selection = TextRange(weightInput.length))
        }
    }

    LaunchedEffect(repsOrDurationInput) {
        if (repsOrDurationInput != repsOrDurationFieldValue.text) {
            repsOrDurationFieldValue = TextFieldValue(
                repsOrDurationInput,
                selection = TextRange(repsOrDurationInput.length)
            )
        }
    }

    LaunchedEffect(weightFieldFocused) {
        if (weightFieldFocused) {
            weightFieldValue = weightFieldValue.copy(
                selection = TextRange(0, weightFieldValue.text.length)
            )
        }
    }

    LaunchedEffect(repsOrDurationFieldFocused) {
        if (repsOrDurationFieldFocused) {
            repsOrDurationFieldValue = repsOrDurationFieldValue.copy(
                selection = TextRange(0, repsOrDurationFieldValue.text.length)
            )
        }
    }

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
            if (allExercises.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_exercises_defined),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
                ) {
                    allExercises.forEach { exercise ->
                        FilterChip(
                            selected = selectedExerciseId == exercise.id,
                            onClick = { onSelectExercise(exercise) },
                            label = { Text(text = exercise.name) }
                        )
                    }
                }

                HorizontalDivider()

                val inputLabel = if (selectedExerciseType == TYPE_REPS)
                    stringResource(R.string.exercise_type_reps)
                else
                    stringResource(R.string.duration_seconds)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = weightFieldValue,
                        onValueChange = {
                            weightFieldValue = it
                            onWeightChange(it.text)
                        },
                        label = { Text(stringResource(R.string.weight_kg)) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Next) }
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .onFocusChanged { state ->
                                weightFieldFocused = state.isFocused
                            }
                    )
                    OutlinedTextField(
                        value = repsOrDurationFieldValue,
                        onValueChange = {
                            repsOrDurationFieldValue = it
                            onRepsOrDurationChange(it.text)
                        },
                        label = { Text(inputLabel) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { focusManager.clearFocus(); onAddSet() }
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .onFocusChanged { state ->
                                repsOrDurationFieldFocused = state.isFocused
                            }
                    )
                    Button(
                        onClick = onAddSet,
                        enabled = !isSavingSet && selectedExerciseId != null,
                        modifier = Modifier.width(64.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(text = "+", style = MaterialTheme.typography.titleLarge)
                    }
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

private const val TYPE_REPS = "reps"
