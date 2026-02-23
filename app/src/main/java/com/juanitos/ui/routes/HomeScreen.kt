package com.juanitos.ui.routes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.juanitos.R
import com.juanitos.ui.AppViewModelProvider
import com.juanitos.ui.commons.MoneySummaryChart
import com.juanitos.ui.navigation.JuanitOSTopAppBar
import com.juanitos.ui.navigation.NavigationDestination
import com.juanitos.ui.navigation.Routes
import com.juanitos.ui.routes.money.MoneyDestination
import com.juanitos.ui.routes.workout.WorkoutDestination

object HomeDestination : NavigationDestination {
    override val route = Routes.Home
    override val titleRes = R.string.app_name
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateTo: (Routes) -> Unit,
    viewModel: HomeViewModel = viewModel(
        factory = AppViewModelProvider.Factory
    )
) {
    val uiState = viewModel.uiState.collectAsState()
    val summary = uiState.value.summary

    Scaffold(topBar = {
        JuanitOSTopAppBar(
            title = stringResource(HomeDestination.titleRes),
            canNavigateBack = false,
        )
    }, bottomBar = {
        BottomAppBar(actions = {
            IconButton(onClick = { onNavigateTo(MoneyDestination.route) }) {
                Icon(
                    painter = painterResource(R.drawable.subapp_money),
                    contentDescription = stringResource(R.string.money)
                )
            }
            IconButton(onClick = { onNavigateTo(WorkoutDestination.route) }) {
                Icon(
                    painter = painterResource(R.drawable.subapp_workout),
                    contentDescription = stringResource(R.string.workout)
                )
            }
        })
    }) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxHeight()
                .padding(
                    start = dimensionResource(R.dimen.padding_small),
                    end = dimensionResource(R.dimen.padding_small)
                ),
        ) {
            // Summary chart
            MoneySummaryChart(summary = summary)
        }
    }
}
