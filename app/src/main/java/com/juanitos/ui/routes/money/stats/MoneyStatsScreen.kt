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
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import com.juanitos.lib.StatsPeriod
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
            if (!uiState.hasSelectedAccount) {
                Text(text = stringResource(R.string.money_stats_no_account))
                return@Column
            }

            PeriodSelector(
                selectedPeriod = uiState.selectedPeriod,
                onPeriodSelected = viewModel::setPeriod,
            )

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
                    IncomeExpenseBarChart(
                        income = uiState.totalIncome,
                        expenses = uiState.totalSpent,
                    )
                }
            }

            if (legendItems.isNotEmpty()) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PeriodSelector(
    selectedPeriod: StatsPeriod,
    onPeriodSelected: (StatsPeriod) -> Unit,
) {
    SingleChoiceSegmentedButtonRow(
        modifier = Modifier.fillMaxWidth(),
    ) {
        StatsPeriod.entries.forEachIndexed { index, period ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = StatsPeriod.entries.size,
                ),
                selected = period == selectedPeriod,
                onClick = { onPeriodSelected(period) },
            ) {
                Text(text = period.label)
            }
        }
    }
}

@Composable
private fun IncomeExpenseBarChart(
    income: Double,
    expenses: Double,
) {
    val incomeColor = MaterialTheme.colorScheme.primary
    val expenseColor = MaterialTheme.colorScheme.error
    val maxValue = maxOf(income, expenses).takeIf { it > 0 } ?: 1.0

    Text(
        text = stringResource(R.string.money_stats_income_expense_title),
        style = MaterialTheme.typography.titleMedium,
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = dimensionResource(R.dimen.padding_small)),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom,
    ) {
        IncomeExpenseBar(value = income, maxValue = maxValue, color = incomeColor)
        IncomeExpenseBar(value = expenses, maxValue = maxValue, color = expenseColor)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small)),
    ) {
        Box(modifier = Modifier.weight(1f)) {
            LegendRow(
                item = MoneyStatsLegendItem(
                    stringResource(R.string.total_income),
                    income,
                    incomeColor
                )
            )
        }
        Box(modifier = Modifier.weight(1f)) {
            LegendRow(
                item = MoneyStatsLegendItem(
                    stringResource(R.string.total_spent),
                    expenses,
                    expenseColor
                )
            )
        }
    }
}

@Composable
private fun IncomeExpenseBar(
    value: Double,
    maxValue: Double,
    color: Color,
) {
    val heightFraction = (value / maxValue).coerceIn(0.0, 1.0).toFloat()
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(width = 60.dp, height = 160.dp * heightFraction)
                .background(color, RoundedCornerShape(8.dp))
        )
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
    val categoryPalette = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.error,
        MaterialTheme.colorScheme.primaryContainer,
        MaterialTheme.colorScheme.secondaryContainer,
        MaterialTheme.colorScheme.tertiaryContainer,
    )

    val categoryColors = slices
        .mapNotNull { it.label }
        .distinct()
        .sorted()
        .mapIndexed { index, name -> name to categoryPalette[index % categoryPalette.size] }
        .toMap()

    return slices.map { slice ->
        val label = slice.label ?: stringResource(R.string.uncategorized)
        val color = if (slice.label != null) {
            categoryColors[slice.label] ?: categoryPalette.first()
        } else {
            categoryPalette.first()
        }

        MoneyStatsLegendItem(
            label = label,
            amount = slice.amount,
            color = color,
        )
    }
}

