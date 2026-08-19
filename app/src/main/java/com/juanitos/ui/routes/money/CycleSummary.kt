package com.juanitos.ui.routes.money

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.juanitos.R
import com.juanitos.lib.MoneyCycleSummary
import java.util.Locale

@Composable
fun CycleSummary(summary: MoneyCycleSummary, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_medium))
    ) {
        Column {
            Text(
                text = stringResource(R.string.total_income),
                style = MaterialTheme.typography.labelLarge
            )
            Text(
                text = formatAmount(summary.totalIncome),
                style = MaterialTheme.typography.headlineMedium
            )
        }

        if (summary.categoryExpenses.isNotEmpty()) {
            HorizontalDivider()
            Column(
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
            ) {
                summary.categoryExpenses.forEach { category ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = category.categoryName)
                        Text(text = formatAmount(category.amount))
                    }
                }
            }
        }

        HorizontalDivider()
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.remaining_money),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = formatAmount(summary.remaining),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (summary.remaining < 0) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                }
            )
        }
    }
}

private fun formatAmount(amount: Double): String = String.format(Locale.US, "%.2f€", amount)
