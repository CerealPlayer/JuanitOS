package com.juanitos.ui.routes.money.goal

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.juanitos.R
import com.juanitos.lib.SavingsGoalPaceStatus
import com.juanitos.lib.SavingsGoalProjection
import java.util.Locale

@Composable
fun SavingsGoalCard(
    projection: SavingsGoalProjection?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier
        .fillMaxWidth()
        .clickable(onClick = onClick)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.padding_medium)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small)),
        ) {
            Text(
                text = stringResource(R.string.savings_goal_card_title),
                style = MaterialTheme.typography.titleMedium,
            )

            if (projection == null) {
                Text(text = stringResource(R.string.savings_goal_card_no_goal))
                return@Column
            }

            if (projection.status == SavingsGoalPaceStatus.NO_TARGET) {
                Text(text = stringResource(R.string.savings_goal_card_no_target))
                return@Column
            }

            val statusColor = if (projection.status == SavingsGoalPaceStatus.AT_RISK) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.primary
            }
            val statusLabel = when (projection.status) {
                SavingsGoalPaceStatus.AT_RISK -> stringResource(R.string.savings_goal_status_at_risk)
                SavingsGoalPaceStatus.AHEAD -> stringResource(R.string.savings_goal_status_ahead)
                else -> stringResource(R.string.savings_goal_status_on_track)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = String.format(
                        Locale.US,
                        "%.2f€ / %.2f€",
                        projection.savedSoFar,
                        projection.target
                    ),
                    fontWeight = FontWeight.Bold,
                )
                Text(text = statusLabel, color = statusColor, fontWeight = FontWeight.SemiBold)
            }

            LinearProgressIndicator(
                progress = {
                    (projection.savedSoFar / projection.target).toFloat().coerceIn(0f, 1f)
                },
                modifier = Modifier.fillMaxWidth(),
                color = statusColor,
            )

            Text(
                text = stringResource(
                    R.string.savings_goal_card_safe_to_spend,
                    String.format(Locale.US, "%.2f€", projection.safeToSpendToday),
                ),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
