package com.juanitos.ui.routes.workout.exercises

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.juanitos.R
import com.juanitos.data.workout.entities.ExerciseDefinition
import com.juanitos.lib.formatDbDatetimeToShortDate
import com.juanitos.ui.AppViewModelProvider
import com.juanitos.ui.navigation.JuanitOSTopAppBar
import com.juanitos.ui.navigation.NavigationDestination
import com.juanitos.ui.navigation.Routes
import com.juanitos.ui.routes.workout.NewWorkoutViewModel
import java.util.Locale

object ExerciseProgressDestination : NavigationDestination {
    override val route = Routes.ExerciseProgress
    override val titleRes = R.string.exercise_progress

    fun createRoute(exerciseId: Int): String = "exercise_progress/$exerciseId"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseProgressScreen(
    onNavigateUp: () -> Unit,
    viewModel: ExerciseProgressViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val uiState = viewModel.uiState.collectAsState().value

    Scaffold(
        topBar = {
            JuanitOSTopAppBar(
                title = stringResource(ExerciseProgressDestination.titleRes),
                canNavigateBack = true,
                navigateUp = onNavigateUp,
            )
        }
    ) { innerPadding ->
        if (!uiState.hasWorkouts) {
            Text(
                text = stringResource(R.string.no_workouts_recorded_for_exercise),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(dimensionResource(R.dimen.padding_small)),
            )
            return@Scaffold
        }

        val exercise = uiState.exercise ?: return@Scaffold
        val isRepsExercise = exercise.type == NewWorkoutViewModel.TYPE_REPS
        val pointLabels = uiState.points.map {
            formatDbDatetimeToShortDate(it.workoutDate).ifBlank { it.workoutDate }
        }
        val weightSeries = uiState.points.mapNotNull { it.weightKg?.toFloat() }
        val repsSeries = uiState.points.mapNotNull { it.reps?.toFloat() }
        val durationSeries = uiState.points.mapNotNull { it.durationSeconds?.toFloat() }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = innerPadding.calculateTopPadding(),
                    bottom = innerPadding.calculateBottomPadding(),
                    start = dimensionResource(R.dimen.padding_small),
                    end = dimensionResource(R.dimen.padding_small),
                ),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small)),
        ) {
            item {
                ExerciseProgressHeader(exercise = exercise)
            }
            if (weightSeries.isNotEmpty()) {
                item {
                    ExerciseProgressChartCard(
                        title = stringResource(R.string.weight_progress),
                        values = weightSeries,
                        labels = pointLabels,
                    )
                }
            }
            if (isRepsExercise && repsSeries.isNotEmpty()) {
                item {
                    ExerciseProgressChartCard(
                        title = stringResource(R.string.reps_progress),
                        values = repsSeries,
                        labels = pointLabels,
                    )
                }
            }
            if (!isRepsExercise && durationSeries.isNotEmpty()) {
                item {
                    ExerciseProgressChartCard(
                        title = stringResource(R.string.duration_progress),
                        values = durationSeries,
                        labels = pointLabels,
                    )
                }
            }
            item {
                Text(
                    text = stringResource(R.string.progress_workout_list_label),
                    style = MaterialTheme.typography.titleSmall,
                )
            }
            items(uiState.points, key = { "${it.workoutDate}-${it.workoutId}" }) { point ->
                Text(
                    text = formatDbDatetimeToShortDate(point.workoutDate).ifBlank { point.workoutDate },
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun ExerciseProgressHeader(exercise: ExerciseDefinition) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(dimensionResource(R.dimen.padding_small)),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = exercise.name,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = stringResource(
                    if (exercise.type == NewWorkoutViewModel.TYPE_REPS) {
                        R.string.exercise_type_reps
                    } else {
                        R.string.exercise_type_duration
                    }
                ),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ExerciseProgressChartCard(title: String, values: List<Float>, labels: List<String>) {
    val maxValue = values.maxOrNull() ?: 0f
    val minValue = values.minOrNull() ?: 0f
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(dimensionResource(R.dimen.padding_small)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small)),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
            )
            Row {
                Column(
                    modifier = Modifier
                        .height(180.dp)
                        .padding(end = 8.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = formatAxisValue(maxValue),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = formatAxisValue(minValue),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                ExerciseProgressLineChart(
                    values = values,
                    modifier = Modifier
                        .weight(1f)
                        .height(180.dp),
                )
            }
            if (labels.isNotEmpty()) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = labels.first(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = labels.last(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.End,
                    )
                }
            }
        }
    }
}

@Composable
private fun ExerciseProgressLineChart(values: List<Float>, modifier: Modifier = Modifier) {
    val strokeColor = MaterialTheme.colorScheme.primary
    val axisColor = MaterialTheme.colorScheme.outline
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .padding(end = 8.dp)
    ) {
        if (size.width <= 0f || size.height <= 0f || values.isEmpty()) return@Canvas

        drawLine(
            color = axisColor,
            start = Offset(0f, size.height),
            end = Offset(size.width, size.height),
            strokeWidth = 2f,
        )
        drawLine(
            color = axisColor,
            start = Offset(0f, 0f),
            end = Offset(0f, size.height),
            strokeWidth = 2f,
        )

        if (values.size == 1) {
            drawCircle(
                color = strokeColor,
                radius = 8f,
                center = Offset(size.width / 2f, size.height / 2f),
            )
            return@Canvas
        }

        val maxValue = values.maxOrNull() ?: 1f
        val minValue = values.minOrNull() ?: 0f
        val range = (maxValue - minValue).takeIf { it > 0f } ?: 1f
        val xStep = size.width / (values.size - 1).toFloat()
        val path = Path()

        values.forEachIndexed { index, value ->
            val normalized = (value - minValue) / range
            val x = xStep * index
            val y = size.height - (normalized * size.height)
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            drawCircle(color = strokeColor, radius = 5f, center = Offset(x, y))
        }

        drawPath(
            path = path,
            color = strokeColor,
            style = Stroke(width = 4f),
        )
    }
}

private fun formatAxisValue(value: Float): String {
    return if (value % 1f == 0f) value.toInt().toString() else String.format(
        Locale.US,
        "%.1f",
        value
    )
}

