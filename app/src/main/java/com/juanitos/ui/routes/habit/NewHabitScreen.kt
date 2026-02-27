package com.juanitos.ui.routes.habit

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.juanitos.R
import com.juanitos.ui.AppViewModelProvider
import com.juanitos.ui.commons.FormColumn
import com.juanitos.ui.navigation.JuanitOSTopAppBar
import com.juanitos.ui.navigation.NavigationDestination
import com.juanitos.ui.navigation.Routes

object NewHabitDestination : NavigationDestination {
    override val route = Routes.NewHabit
    override val titleRes = R.string.new_habit
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewHabitScreen(
    onNavigateUp: () -> Unit,
    viewModel: NewHabitViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val uiState = viewModel.uiState.collectAsState().value

    Scaffold(topBar = {
        JuanitOSTopAppBar(
            title = stringResource(NewHabitDestination.titleRes),
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
            if (uiState.errorMessage != null) {
                Text(text = uiState.errorMessage, color = Color.Red)
            }
            Button(
                onClick = { viewModel.saveHabit(onNavigateUp) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSaving
            ) {
                Text(text = stringResource(R.string.save))
            }
        }
    }
}
