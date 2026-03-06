package com.juanitos.ui.routes.climbing.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.juanitos.R
import com.juanitos.ui.navigation.JuanitOSTopAppBar
import com.juanitos.ui.navigation.NavigationDestination
import com.juanitos.ui.navigation.Routes

object ClimbingWorkoutDetailDestination : NavigationDestination {
    override val route = Routes.ClimbingWorkoutDetail
    override val titleRes = R.string.climbing_workout_detail

    fun createRoute(workoutId: Int): String = "climbing_workout_detail/$workoutId"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClimbingWorkoutDetailScreen(
    onNavigateUp: () -> Unit,
) {
    Scaffold(
        topBar = {
            JuanitOSTopAppBar(
                title = stringResource(ClimbingWorkoutDetailDestination.titleRes),
                canNavigateBack = true,
                navigateUp = onNavigateUp,
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = stringResource(R.string.climbing_workout_detail_placeholder))
        }
    }
}
