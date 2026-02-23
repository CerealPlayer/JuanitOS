package com.juanitos.ui.commons

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.juanitos.R
import com.juanitos.ui.routes.MoneySummary
import java.util.Locale
import kotlin.math.abs

@Composable
fun MoneySummaryChart(summary: MoneySummary) {
    val totalExpenses = summary.totalFixedSpendings + summary.totalTransactions
    val maxValue = maxOf(
        summary.totalIncome,
        totalExpenses,
        abs(summary.remaining)
    ).takeIf { it > 0 } ?: 1.0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(16.dp),
    ) {
        // Title
        Text(
            text = stringResource(R.string.money_summary_title),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        // Chart bars with labels and values
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            // Income column
            BarChart(
                value = summary.totalIncome,
                maxValue = maxValue,
                color = MaterialTheme.colorScheme.primary
            )

            // Expenses column
            BarChart(
                value = totalExpenses,
                maxValue = maxValue,
                color = MaterialTheme.colorScheme.secondary
            )

            // Remaining column
            val remainingColor = if (summary.remaining >= 0) {
                MaterialTheme.colorScheme.tertiary
            } else {
                MaterialTheme.colorScheme.errorContainer
            }
            BarChart(
                value = abs(summary.remaining),
                maxValue = maxValue,
                color = remainingColor
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            LegendItem(
                label = stringResource(R.string.total_income),
                value = String.format(Locale.US, "%.2f", summary.totalIncome),
                color = MaterialTheme.colorScheme.primary
            )
            LegendItem(
                label = stringResource(R.string.total_spent),
                value = String.format(Locale.US, "%.2f", totalExpenses),
                color = MaterialTheme.colorScheme.secondary
            )
            LegendItem(
                label = stringResource(R.string.remaining_money),
                value = String.format(Locale.US, "%.2f", summary.remaining),
                color = if (summary.remaining >= 0) {
                    MaterialTheme.colorScheme.tertiary
                } else {
                    MaterialTheme.colorScheme.errorContainer
                }
            )
        }
    }
}

@Composable
private fun BarChart(
    value: Double,
    maxValue: Double,
    color: Color
) {
    // Bar
    val heightFraction = (value / maxValue).coerceIn(0.0, 1.0).toFloat()
    Column(
        modifier = Modifier
            .width(60.dp)
            .height(120.dp * heightFraction)
            .background(
                color = color,
                shape = RoundedCornerShape(8.dp)
            )
    ) {}
}

@Composable
private fun LegendItem(
    label: String,
    value: String,
    color: Color
) {
    Column(
        modifier = Modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .padding(end = 4.dp)
                    .height(12.dp)
                    .width(12.dp)
                    .background(
                        color = color,
                        shape = RoundedCornerShape(2.dp)
                    )
            )
            Text(
                text = label,
                fontSize = 10.sp
            )
        }
//        }
        Text(
            text = value,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}





