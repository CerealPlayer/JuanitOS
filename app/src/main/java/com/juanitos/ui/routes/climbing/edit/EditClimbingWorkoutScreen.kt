package com.juanitos.ui.routes.climbing.edit

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.juanitos.R
import com.juanitos.ui.AppViewModelProvider
import com.juanitos.ui.navigation.JuanitOSTopAppBar
import com.juanitos.ui.navigation.NavigationDestination
import com.juanitos.ui.navigation.Routes

object EditClimbingWorkoutDestination : NavigationDestination {
    override val route = Routes.EditClimbingWorkout
    override val titleRes = R.string.edit_climbing_workout

    fun createRoute(workoutId: Int): String = "edit_climbing_workout/$workoutId"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditClimbingWorkoutScreen(
    onNavigateUp: () -> Unit,
    onNewBoulder: () -> Unit,
    viewModel: EditClimbingWorkoutViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val handleBack: () -> Unit = {
        if (uiState.hasChanges) {
            viewModel.openDiscardDialog()
        } else {
            onNavigateUp()
        }
    }
    val boulderActionLabel = if (uiState.selectedBoulder == null) {
        stringResource(R.string.add_boulder)
    } else {
        stringResource(R.string.select_new_boulder)
    }
    val bouldersById = remember(uiState.boulders) { uiState.boulders.associateBy { it.id } }
    val boulderSectionIds = remember(uiState.selectedBoulderIds, uiState.attemptsByBoulderId) {
        buildList {
            addAll(uiState.selectedBoulderIds)
            uiState.attemptsByBoulderId.keys.forEach { if (!contains(it)) add(it) }
        }
    }
    val galleryPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> uri?.let(viewModel::addAttemptFromUri) }
    )

    BackHandler(onBack = handleBack)

    Scaffold(
        topBar = {
            JuanitOSTopAppBar(
                title = stringResource(EditClimbingWorkoutDestination.titleRes),
                canNavigateBack = true,
                navigateUp = handleBack,
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .imePadding()
                .padding(dimensionResource(R.dimen.padding_small))
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                value = uiState.notes,
                onValueChange = viewModel::setNotes,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.initial_notes)) },
                enabled = uiState.isLoaded && !uiState.isSaving,
            )
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_small)))
            if (uiState.selectedBoulder == null) {
                EditBoulderActionCard(
                    label = boulderActionLabel,
                    onClick = viewModel::openBoulderDialog,
                )
            }
            boulderSectionIds.forEach { boulderId ->
                val sectionBoulder = bouldersById[boulderId] ?: return@forEach
                val sectionAttempts = uiState.attemptsByBoulderId[boulderId].orEmpty()
                val isSelectedSection = boulderId == uiState.selectedBoulderId
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_small)))
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(dimensionResource(R.dimen.padding_small)),
                        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small)),
                    ) {
                        EditBoulderImage(
                            imagePath = sectionBoulder.imagePath,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(dimensionResource(R.dimen.boulder_card_image_height))
                        )
                        Text(
                            text = stringResource(
                                R.string.selected_boulder_grade,
                                sectionBoulder.grade
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_small)))
                sectionAttempts.forEachIndexed { index, attempt ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(dimensionResource(R.dimen.padding_small)),
                            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small)),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = stringResource(R.string.attempt_label, index + 1),
                                    modifier = Modifier.weight(1f),
                                )
                                IconButton(
                                    onClick = { viewModel.removeAttempt(attempt.id) },
                                    enabled = !uiState.isSaving && isSelectedSection,
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.delete),
                                        contentDescription = stringResource(R.string.remove_attempt),
                                    )
                                }
                            }
                            EditAttemptVideoPreview(
                                context = context,
                                videoPath = attempt.videoPath,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(dimensionResource(R.dimen.boulder_card_image_height)),
                            )
                            OutlinedTextField(
                                value = attempt.notes,
                                onValueChange = { viewModel.setAttemptNotes(attempt.id, it) },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text(stringResource(R.string.attempt_notes_optional)) },
                                minLines = 2,
                                enabled = !uiState.isSaving && isSelectedSection,
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_small)))
                }
                if (isSelectedSection) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(dimensionResource(R.dimen.padding_small)),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = stringResource(R.string.add_attempt),
                                modifier = Modifier.weight(1f),
                            )
                            IconButton(
                                onClick = {
                                    galleryPicker.launch(
                                        PickVisualMediaRequest(
                                            mediaType = ActivityResultContracts.PickVisualMedia.VideoOnly
                                        )
                                    )
                                },
                                enabled = !uiState.isSaving,
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.add),
                                    contentDescription = stringResource(R.string.select_video),
                                )
                            }
                        }
                    }
                }
            }
            if (uiState.selectedBoulder != null) {
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_small)))
                EditBoulderActionCard(
                    label = boulderActionLabel,
                    onClick = viewModel::openBoulderDialog,
                )
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_small)))
                val errorMessage = uiState.errorMessage
                if (errorMessage != null) {
                    Text(text = errorMessage, color = Color.Red)
                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_small)))
                }
                Button(
                    onClick = { viewModel.saveWorkout(onNavigateUp) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState.canSave,
                ) {
                    Text(text = stringResource(R.string.save_changes))
                }
            }
        }
    }

    if (uiState.showBoulderDialog) {
        AlertDialog(
            modifier = Modifier.fillMaxWidth(0.95f),
            properties = DialogProperties(usePlatformDefaultWidth = false),
            onDismissRequest = viewModel::closeBoulderDialog,
            title = { Text(text = stringResource(R.string.select_boulder)) },
            text = {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small)),
                    verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small)),
                    modifier = Modifier.heightIn(max = dimensionResource(R.dimen.dialog_grid_max_height)),
                ) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.closeBoulderDialog()
                                    onNewBoulder()
                                }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(dimensionResource(R.dimen.padding_small)),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small)),
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.add),
                                    contentDescription = stringResource(R.string.new_boulder),
                                    modifier = Modifier.size(dimensionResource(R.dimen.padding_medium) * 2)
                                )
                                Text(text = stringResource(R.string.new_boulder))
                            }
                        }
                    }
                    items(uiState.boulders, key = { it.id }) { boulder ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.selectBoulder(boulder.id) }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(dimensionResource(R.dimen.padding_small)),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small)),
                            ) {
                                EditBoulderImage(
                                    imagePath = boulder.imagePath,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(dimensionResource(R.dimen.boulder_card_image_height))
                                )
                                Text(
                                    text = boulder.grade,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = viewModel::closeBoulderDialog) {
                    Text(text = stringResource(R.string.cancel))
                }
            }
        )
    }

    if (uiState.showDiscardDialog) {
        AlertDialog(
            onDismissRequest = viewModel::closeDiscardDialog,
            title = { Text(stringResource(R.string.discard_climbing_workout_changes)) },
            text = { Text(stringResource(R.string.discard_climbing_workout_changes_message)) },
            confirmButton = {
                Button(onClick = { viewModel.discardChanges(onNavigateUp) }) {
                    Text(stringResource(R.string.discard))
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::closeDiscardDialog) {
                    Text(stringResource(R.string.keep_editing))
                }
            }
        )
    }
}

@Composable
private fun EditBoulderActionCard(
    label: String,
    onClick: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.padding_small)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onClick) {
                Icon(
                    painter = painterResource(R.drawable.add),
                    contentDescription = label,
                )
            }
        }
    }
}

@Composable
private fun EditAttemptVideoPreview(
    context: android.content.Context,
    videoPath: String?,
    modifier: Modifier = Modifier,
) {
    val bitmap: Bitmap? = remember(videoPath) {
        val path = videoPath ?: return@remember null
        runCatching {
            MediaMetadataRetriever().use { retriever ->
                retriever.setDataSource(context, Uri.fromFile(java.io.File(path)))
                retriever.frameAtTime
            }
        }.getOrNull()
    }
    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            modifier = modifier,
        )
    } else {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(R.drawable.media),
                contentDescription = stringResource(R.string.select_video),
                modifier = Modifier.size(dimensionResource(R.dimen.padding_medium) * 2),
            )
        }
    }
}

@Composable
private fun EditBoulderImage(
    imagePath: String?,
    modifier: Modifier = Modifier,
) {
    val bitmap = remember(imagePath) {
        imagePath?.let { path -> BitmapFactory.decodeFile(path) }
    }
    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            modifier = modifier,
        )
    } else {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(R.drawable.media),
                contentDescription = stringResource(R.string.boulder_picture_optional),
                modifier = Modifier.size(dimensionResource(R.dimen.padding_medium) * 2),
            )
        }
    }
}
