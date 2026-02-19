package com.juanitos.ui.routes.money.categories

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

object NewCategoryDestination : NavigationDestination {
    override val route = Routes.NewCategory
    override val titleRes = R.string.new_category
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewCategoryScreen(
    onNavigateUp: () -> Unit,
    viewModel: NewCategoryViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState = viewModel.uiState.collectAsState().value
    Scaffold(topBar = {
        JuanitOSTopAppBar(
            title = stringResource(NewCategoryDestination.titleRes),
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
                label = { Text(text = stringResource(R.string.description)) },
                singleLine = false,
                modifier = Modifier.fillMaxWidth()
            )
            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage,
                    color = Color.Red
                )
            }
            Button(
                onClick = {
                    viewModel.saveCategory(onNavigateUp)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSaving
            ) {
                Text(stringResource(R.string.save))
            }
        }
    }
}

