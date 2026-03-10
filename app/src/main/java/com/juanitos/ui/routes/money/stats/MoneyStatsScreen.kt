package com.juanitos.ui.routes.money.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.juanitos.R
import com.juanitos.ui.AppViewModelProvider
import com.juanitos.ui.navigation.JuanitOSTopAppBar
import com.juanitos.ui.navigation.NavigationDestination
import com.juanitos.ui.navigation.Routes
import java.util.Locale

object MoneyStatsDestination : NavigationDestination {
    override val route = Routes.MoneyStats
    override val titleRes = R.string.money_stats
}

private data class MoneyStatsLegendItem(
    val label: String,
    val amount: Double,
    val color: Color,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoneyStatsScreen(
    onNavigateUp: () -> Unit,
    viewModel: MoneyStatsViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            JuanitOSTopAppBar(
                title = stringResource(MoneyStatsDestination.titleRes),
                canNavigateBack = true,
                navigateUp = onNavigateUp,
            )
        }
    ) { innerPadding ->
        val legendItems = rememberLegendItems(uiState.slices)
        Column(
            modifier = Modifier
                .padding(
                    top = innerPadding.calculateTopPadding(),
                    bottom = innerPadding.calculateBottomPadding(),
                    start = dimensionResource(R.dimen.padding_small),
                    end = dimensionResource(R.dimen.padding_small),
                )
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_medium)),
        ) {
            if (!uiState.hasActiveCycle) {
                Text(text = stringResource(R.string.money_stats_no_active_cycle))
                return@Column
            }

            if (!uiState.hasData) {
                Text(text = stringResource(R.string.money_stats_no_data))
                return@Column
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(dimensionResource(R.dimen.padding_medium)),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small)),
                ) {
                    PieChart(
                        entries = legendItems,
                        total = uiState.totalSpent,
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small)),
            ) {
                itemsIndexed(
                    legendItems,
                    key = { index, item -> "${item.label}-$index" }) { _, item ->
                    LegendRow(item = item)
                }
            }
        }
    }
}

@Composable
private fun PieChart(
    entries: List<MoneyStatsLegendItem>,
    total: Double,
) {
    Canvas(modifier = Modifier.size(240.dp)) {
        var startAngle = -90f
        entries.forEach { entry ->
            val sweepAngle = ((entry.amount / total) * 360f).toFloat()
            drawArc(
                color = entry.color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                style = Fill,
                size = Size(size.width, size.height),
            )
            startAngle += sweepAngle
        }
    }
}

@Composable
private fun LegendRow(item: MoneyStatsLegendItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small)),
        ) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .background(item.color, RoundedCornerShape(3.dp))
            )
            Text(
                text = item.label,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Text(
            text = String.format(Locale.US, "%.2f€", item.amount),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun rememberLegendItems(
    slices: List<MoneyStatsSlice>,
): List<MoneyStatsLegendItem> {
    val fixedSpendingColor = MaterialTheme.colorScheme.secondary
    val categoryPalette = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.error,
        MaterialTheme.colorScheme.primaryContainer,
        MaterialTheme.colorScheme.secondaryContainer,
        MaterialTheme.colorScheme.tertiaryContainer,
    )

    val categoryColors = slices
        .filter { it.type == MoneyStatsSliceType.TransactionCategory }
        .mapNotNull { it.label }
        .distinct()
        .sorted()
        .mapIndexed { index, name -> name to categoryPalette[index % categoryPalette.size] }
        .toMap()

    return slices.map { slice ->
        val label = when (slice.type) {
            MoneyStatsSliceType.FixedSpending -> stringResource(R.string.fixed_spendings)
            MoneyStatsSliceType.TransactionCategory -> slice.label
                ?: stringResource(R.string.uncategorized)
        }

        val color = when (slice.type) {
            MoneyStatsSliceType.FixedSpending -> fixedSpendingColor
            MoneyStatsSliceType.TransactionCategory -> {
                if (slice.label != null) {
                    categoryColors[slice.label] ?: MaterialTheme.colorScheme.primary
                } else {
                    categoryPalette.first()
                }
            }
        }

        MoneyStatsLegendItem(
            label = label,
            amount = slice.amount,
            color = color,
        )
    }
}

