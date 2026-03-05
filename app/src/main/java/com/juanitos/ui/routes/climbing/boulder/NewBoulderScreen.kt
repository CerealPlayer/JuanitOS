package com.juanitos.ui.routes.climbing.boulder

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.juanitos.R
import com.juanitos.ui.AppViewModelProvider
import com.juanitos.ui.commons.FormColumn
import com.juanitos.ui.navigation.JuanitOSTopAppBar
import com.juanitos.ui.navigation.NavigationDestination
import com.juanitos.ui.navigation.Routes

object NewBoulderDestination : NavigationDestination {
    override val route = Routes.NewBoulder
    override val titleRes = R.string.new_boulder
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewBoulderScreen(
    onNavigateUp: () -> Unit,
    viewModel: NewBoulderViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val uiState = viewModel.uiState.collectAsState().value
    val galleryPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> viewModel.setSelectedImageUri(uri) }
    )

    Scaffold(
        topBar = {
            JuanitOSTopAppBar(
                title = stringResource(NewBoulderDestination.titleRes),
                canNavigateBack = true,
                navigateUp = onNavigateUp,
            )
        },
    ) { innerPadding ->
        FormColumn(innerPadding) {
            OutlinedTextField(
                value = uiState.gradeInput,
                onValueChange = viewModel::setGradeInput,
                label = { Text(text = stringResource(R.string.grade)) },
                isError = !uiState.isGradeValid,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = uiState.styleInput,
                onValueChange = viewModel::setStyleInput,
                label = { Text(text = stringResource(R.string.style_optional)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = stringResource(R.string.boulder_picture_optional))
                IconButton(
                    onClick = {
                        galleryPicker.launch(
                            PickVisualMediaRequest(
                                mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    },
                    enabled = !uiState.isSaving,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.media),
                        contentDescription = stringResource(R.string.select_image),
                    )
                }
            }
            if (uiState.selectedImageUri != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Button(
                        onClick = { viewModel.setSelectedImageUri(null) },
                        enabled = !uiState.isSaving,
                    ) {
                        Text(text = stringResource(R.string.remove_image))
                    }
                }
            }
            if (uiState.errorMessage != null) {
                Text(text = uiState.errorMessage, color = Color.Red)
            }
            Button(
                onClick = { viewModel.saveBoulder(onNavigateUp) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSaving,
            ) {
                Text(text = stringResource(R.string.save))
            }
        }
    }
}
