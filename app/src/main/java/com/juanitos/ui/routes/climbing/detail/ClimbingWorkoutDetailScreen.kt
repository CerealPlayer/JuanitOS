package com.juanitos.ui.routes.climbing.detail

import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.MediaController
import android.widget.VideoView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.juanitos.R
import com.juanitos.ui.AppViewModelProvider
import com.juanitos.ui.commons.DeleteConfirmationDialog
import com.juanitos.ui.icons.MoreVert
import com.juanitos.ui.navigation.JuanitOSTopAppBar
import com.juanitos.ui.navigation.NavigationDestination
import com.juanitos.ui.navigation.Routes
import java.io.File

object ClimbingWorkoutDetailDestination : NavigationDestination {
    override val route = Routes.ClimbingWorkoutDetail
    override val titleRes = R.string.climbing_workout_detail

    fun createRoute(workoutId: Int): String = "climbing_workout_detail/$workoutId"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClimbingWorkoutDetailScreen(
    onNavigateUp: () -> Unit,
    onEditWorkout: (Int) -> Unit,
    viewModel: ClimbingWorkoutDetailViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    data class AttemptDialogUiState(
        val attempt: ClimbingWorkoutAttemptDetailUiState,
        val attemptNumber: Int,
    )

    val uiState = viewModel.uiState.collectAsState().value
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    var selectedAttempt by remember { mutableStateOf<AttemptDialogUiState?>(null) }
    val showMenu = remember { mutableStateOf(false) }
    val showDeleteConfirmation = remember { mutableStateOf(false) }

    if (showDeleteConfirmation.value) {
        DeleteConfirmationDialog(
            title = stringResource(R.string.confirm_delete_workout),
            onConfirm = {
                showDeleteConfirmation.value = false
                uiState.workout?.let { workout ->
                    onNavigateUp()
                    viewModel.deleteWorkout(workout)
                }
            },
            onDismiss = { showDeleteConfirmation.value = false },
        )
    }

    Scaffold(
        topBar = {
            JuanitOSTopAppBar(
                title = stringResource(ClimbingWorkoutDetailDestination.titleRes),
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
                                text = { Text(stringResource(R.string.edit)) },
                                onClick = {
                                    showMenu.value = false
                                    uiState.workout?.id?.let(onEditWorkout)
                                },
                            )
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
        },
    ) { innerPadding ->
        if (!uiState.workoutFound) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = stringResource(R.string.climbing_workout_not_found))
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = innerPadding.calculateTopPadding(),
                    bottom = innerPadding.calculateBottomPadding(),
                    start = dimensionResource(R.dimen.padding_small),
                    end = dimensionResource(R.dimen.padding_small),
                ),
        ) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = dimensionResource(R.dimen.padding_small)),
                ) {
                    Column(modifier = Modifier.padding(dimensionResource(R.dimen.padding_small))) {
                        Text(
                            text = stringResource(R.string.initial_notes),
                            style = MaterialTheme.typography.titleSmall,
                        )
                        Text(
                            text = uiState.notes?.takeIf { it.isNotBlank() }
                                ?: stringResource(R.string.climbing_no_initial_notes),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
            }

            items(uiState.sections, key = { it.boulderId }) { section ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = dimensionResource(R.dimen.padding_small)),
                ) {
                    Column(modifier = Modifier.padding(dimensionResource(R.dimen.padding_small))) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = stringResource(R.string.boulder_grade_label, section.grade),
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.weight(1f),
                            )
                            TextButton(onClick = { viewModel.toggleBoulderSection(section.boulderId) }) {
                                Text(
                                    text = if (section.isExpanded) {
                                        stringResource(R.string.hide_boulder)
                                    } else {
                                        stringResource(R.string.show_boulder)
                                    }
                                )
                            }
                        }

                        if (section.isExpanded) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = screenHeight * 0.72f)
                                    .verticalScroll(rememberScrollState()),
                            ) {
                                if (!section.style.isNullOrBlank()) {
                                    Text(
                                        text = stringResource(
                                            R.string.boulder_style_label,
                                            section.style
                                        ),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(bottom = 4.dp),
                                    )
                                }

                                BoulderImage(
                                    imagePath = section.imagePath,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(screenHeight * 0.52f),
                                )

                                if (section.attempts.isEmpty()) {
                                    Text(
                                        text = stringResource(R.string.no_attempts_for_boulder),
                                        modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_small)),
                                    )
                                } else {
                                    section.attempts.forEachIndexed { index, attempt ->
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = dimensionResource(R.dimen.padding_small)),
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(dimensionResource(R.dimen.padding_small)),
                                                verticalAlignment = Alignment.CenterVertically,
                                            ) {
                                                Text(
                                                    text = stringResource(
                                                        R.string.attempt_label,
                                                        index + 1
                                                    ),
                                                    modifier = Modifier.weight(1f),
                                                )
                                                TextButton(
                                                    onClick = {
                                                        selectedAttempt = AttemptDialogUiState(
                                                            attempt = attempt,
                                                            attemptNumber = index + 1,
                                                        )
                                                    },
                                                    colors = ButtonDefaults.textButtonColors(),
                                                ) {
                                                    Text(stringResource(R.string.view_attempt))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        val dialogAttempt = selectedAttempt
        if (dialogAttempt != null) {
            Dialog(
                onDismissRequest = { selectedAttempt = null },
                properties = DialogProperties(usePlatformDefaultWidth = false),
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .fillMaxHeight(0.9f),
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(dimensionResource(R.dimen.padding_small)),
                    ) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = stringResource(
                                        R.string.attempt_label,
                                        dialogAttempt.attemptNumber
                                    ),
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.weight(1f),
                                )
                                IconButton(onClick = { selectedAttempt = null }) {
                                    Icon(
                                        painter = painterResource(R.drawable.close),
                                        contentDescription = stringResource(R.string.close),
                                    )
                                }
                            }
                        }
                        item {
                            if (dialogAttempt.attempt.videoPath.isNullOrBlank()) {
                                Text(
                                    text = stringResource(R.string.no_video_for_attempt),
                                    modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_small)),
                                )
                            } else {
                                AttemptVideoPlayer(
                                    videoPath = dialogAttempt.attempt.videoPath,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(screenHeight * 0.68f)
                                        .padding(top = dimensionResource(R.dimen.padding_small)),
                                )
                            }
                        }
                        item {
                            Text(
                                text = dialogAttempt.attempt.notes?.takeIf { it.isNotBlank() }
                                    ?: stringResource(R.string.no_attempt_notes),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_small)),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BoulderImage(
    imagePath: String?,
    modifier: Modifier = Modifier,
) {
    val bitmap = remember(imagePath) { imagePath?.let { BitmapFactory.decodeFile(it) } }
    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            modifier = modifier,
        )
    } else {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            androidx.compose.material3.Icon(
                painter = painterResource(R.drawable.media),
                contentDescription = stringResource(R.string.boulder_picture_optional),
            )
        }
    }
}

@Composable
private fun AttemptVideoPlayer(
    videoPath: String,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val videoUri = remember(videoPath) { Uri.fromFile(File(videoPath)) }
    AndroidView(
        modifier = modifier,
        factory = {
            VideoView(context).apply {
                val mediaController = MediaController(context)
                mediaController.setAnchorView(this)
                setMediaController(mediaController)
                setVideoURI(videoUri)
                seekTo(1)
            }
        },
        update = { view ->
            view.setVideoURI(videoUri)
            view.seekTo(1)
            view.start()
        },
    )
}
