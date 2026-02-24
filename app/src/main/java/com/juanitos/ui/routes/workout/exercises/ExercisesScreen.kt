package com.juanitos.ui.routes.workout.exercises

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.juanitos.R
import com.juanitos.data.workout.entities.ExerciseDefinition
import com.juanitos.ui.AppViewModelProvider
import com.juanitos.ui.commons.DeleteConfirmationDialog
import com.juanitos.ui.icons.Add
import com.juanitos.ui.navigation.JuanitOSTopAppBar
import com.juanitos.ui.navigation.NavigationDestination
import com.juanitos.ui.navigation.Routes
import kotlinx.coroutines.launch

object ExercisesDestination : NavigationDestination {
    override val route = Routes.Exercises
    override val titleRes = R.string.exercise_definitions
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExercisesScreen(
    onNavigateUp: () -> Unit,
    onNewExercise: () -> Unit,
    viewModel: ExercisesViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val exercises = viewModel.uiState.collectAsState().value.exercises

    Scaffold(
        topBar = {
            JuanitOSTopAppBar(
                title = stringResource(ExercisesDestination.titleRes),
                canNavigateBack = true,
                navigateUp = onNavigateUp
            )
        },
        bottomBar = {
            BottomAppBar(
                actions = {},
                floatingActionButton = {
                    FloatingActionButton(onClick = onNewExercise) {
                        Add()
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(
                    top = innerPadding.calculateTopPadding(),
                    bottom = innerPadding.calculateBottomPadding(),
                    start = dimensionResource(R.dimen.padding_small),
                    end = dimensionResource(R.dimen.padding_small)
                ),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
        ) {
            items(exercises, key = { it.id }) { exercise ->
                ExerciseCard(
                    exercise = exercise,
                    onDelete = { viewModel.deleteExercise(it) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseCard(
    exercise: ExerciseDefinition,
    onDelete: (ExerciseDefinition) -> Unit,
    modifier: Modifier = Modifier,
) {
    val dismissState = rememberSwipeToDismissBoxState()
    val showDeleteConfirmation = remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
            showDeleteConfirmation.value = true
        }
    }

    if (showDeleteConfirmation.value) {
        DeleteConfirmationDialog(
            title = stringResource(R.string.confirm_delete_exercise),
            onConfirm = { onDelete(exercise) },
            onDismiss = {
                showDeleteConfirmation.value = false
                coroutineScope.launch { dismissState.reset() }
            }
        )
    }

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
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
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(dimensionResource(R.dimen.padding_small)),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = exercise.name,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )
                    val typeLabel = if (exercise.type == "reps") {
                        stringResource(R.string.exercise_type_reps)
                    } else {
                        stringResource(R.string.exercise_type_duration)
                    }
                    SuggestionChip(
                        onClick = {},
                        label = { Text(text = typeLabel) }
                    )
                }
                if (!exercise.description.isNullOrBlank()) {
                    Text(
                        text = exercise.description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
