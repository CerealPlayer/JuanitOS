package com.juanitos.ui.routes.food.track

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.juanitos.R
import com.juanitos.ui.AppViewModelProvider
import com.juanitos.ui.navigation.JuanitOSTopAppBar
import com.juanitos.ui.navigation.NavigationDestination
import com.juanitos.ui.navigation.Routes
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries

object TrackDestination : NavigationDestination {
    override val route = Routes.Track
    override val titleRes = R.string.tracking
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackScreen(
    onNavigateUp: () -> Unit,
    viewModel: TrackViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val dailyData = viewModel.dailyData.collectAsState().value
    val calData = dailyData.map { it.totalCalories }
    val protData = dailyData.map { it.totalProteins }

    val modelProducer = remember { CartesianChartModelProducer() }
    LaunchedEffect(calData, protData) {
        modelProducer.runTransaction {
            lineSeries {
                series(calData.ifEmpty { listOf(0) })
                series(protData.ifEmpty { listOf(0) })
            }
        }
    }

    Scaffold(
        topBar = {
            JuanitOSTopAppBar(
                title = stringResource(TrackDestination.titleRes),
                canNavigateBack = true,
                navigateUp = onNavigateUp
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(
                    top = innerPadding.calculateTopPadding(),
                    bottom = innerPadding.calculateBottomPadding(),
                    start = dimensionResource(R.dimen.padding_small),
                    end = dimensionResource(R.dimen.padding_small)
                )
        ) {
            CartesianChartHost(
                rememberCartesianChart(
                    rememberLineCartesianLayer(),
                    startAxis = VerticalAxis.rememberStart(),
                    bottomAxis = HorizontalAxis.rememberBottom(),
                ),
                modelProducer,
            )
        }
    }
}