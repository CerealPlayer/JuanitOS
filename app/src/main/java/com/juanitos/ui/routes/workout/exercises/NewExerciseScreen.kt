package com.juanitos.ui.routes.workout.exercises

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.lifecycle.viewmodel.compose.viewModel
import com.juanitos.R
import com.juanitos.ui.AppViewModelProvider
import com.juanitos.ui.commons.FormColumn
import com.juanitos.ui.navigation.JuanitOSTopAppBar
import com.juanitos.ui.navigation.NavigationDestination
import com.juanitos.ui.navigation.Routes
import com.juanitos.ui.routes.workout.exercises.NewExerciseUiState.Companion.TYPE_DURATION
import com.juanitos.ui.routes.workout.exercises.NewExerciseUiState.Companion.TYPE_REPS

object NewExerciseDestination : NavigationDestination {
    override val route = Routes.NewExercise
    override val titleRes = R.string.new_exercise
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewExerciseScreen(
    onNavigateUp: () -> Unit,
    viewModel: NewExerciseViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState = viewModel.uiState.collectAsState().value

    Scaffold(topBar = {
        JuanitOSTopAppBar(
            title = stringResource(NewExerciseDestination.titleRes),
            canNavigateBack = true,
            navigateUp = onNavigateUp
        )
    }) { innerPadding ->
        FormColumn(innerPadding) {
            OutlinedTextField(
                value = uiState.nameInput,
                onValueChange = { viewModel.setNameInput(it) },
                label = { Text(text = stringResource(R.string.name)) },
                isError = !uiState.isNameValid,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = uiState.descriptionInput,
                onValueChange = { viewModel.setDescriptionInput(it) },
                label = { Text(text = stringResource(R.string.description_optional)) },
                singleLine = false,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = stringResource(R.string.exercise_type),
                style = MaterialTheme.typography.labelLarge
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectableGroup(),
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
            ) {
                listOf(
                    TYPE_REPS to R.string.exercise_type_reps,
                    TYPE_DURATION to R.string.exercise_type_duration
                )
                    .forEach { (type, labelRes) ->
                        Row(
                            modifier = Modifier
                                .selectable(
                                    selected = uiState.typeInput == type,
                                    onClick = { viewModel.setTypeInput(type) },
                                    role = Role.RadioButton
                                )
                                .padding(end = dimensionResource(R.dimen.padding_small)),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = uiState.typeInput == type,
                                onClick = null
                            )
                            Text(text = stringResource(labelRes))
                        }
                    }
            }
            if (uiState.errorMessage != null) {
                Text(text = uiState.errorMessage, color = Color.Red)
            }
            Button(
                onClick = { viewModel.saveExercise(onNavigateUp) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSaving
            ) {
                Text(text = stringResource(R.string.save))
            }
        }
    }
}
