package com.juanitos.ui.routes.money

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.juanitos.R
import com.juanitos.data.money.entities.relations.FixedSpendingWithCategory
import com.juanitos.ui.commons.DeleteConfirmationDialog
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FixedSpendingCard(
    fixedSpendingWithCategory: FixedSpendingWithCategory,
    modifier: Modifier = Modifier,
    onDelete: ((FixedSpendingWithCategory) -> Unit)? = null,
    onFixedSpendingCheck: ((Boolean) -> Unit)? = null,
) {
    val isEditable = onDelete != null && onFixedSpendingCheck != null
    if (isEditable) {
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
                title = stringResource(R.string.confirm_delete_fixed_spending),
                onConfirm = {
                    onDelete(fixedSpendingWithCategory)
                },
                onDismiss = {
                    showDeleteConfirmation.value = false
                    coroutineScope.launch {
                        dismissState.reset()
                    }
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
                    contentAlignment = if (dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd) {
                        Alignment.CenterStart
                    } else {
                        Alignment.CenterEnd
                    }
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
            FixedSpendingCardContent(
                fixedSpending = fixedSpendingWithCategory,
                onFixedSpendingCheck = onFixedSpendingCheck
            )
        }
    } else {
        // Without swipe-to-delete capability
        FixedSpendingCardContent(
            fixedSpending = fixedSpendingWithCategory,
            onFixedSpendingCheck = onFixedSpendingCheck,
            modifier = modifier
        )
    }
}

@Composable
private fun FixedSpendingCardContent(
    fixedSpending: FixedSpendingWithCategory,
    modifier: Modifier = Modifier,
    onFixedSpendingCheck: ((Boolean) -> Unit)? = null,
) {
    val isEditable = onFixedSpendingCheck != null
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.padding_small)),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = if (!isEditable) Modifier.fillMaxWidth() else Modifier,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = String.format(
                            Locale.US,
                            "%.2f€",
                            fixedSpending.fixedSpending.amount
                        )
                    )
                    if (isEditable) {
                        Text(text = fixedSpending.category.name)
                    }
                    if (!fixedSpending.fixedSpending.description.isNullOrBlank()) {
                        Text(
                            text = fixedSpending.fixedSpending.description,
                        )
                    }
                }
                if (!isEditable) {
                    Text(text = fixedSpending.category.name)
                }
            }
            if (onFixedSpendingCheck != null) {
                Checkbox(
                    checked = fixedSpending.fixedSpending.active,
                    onCheckedChange = onFixedSpendingCheck
                )
            }
        }
    }
}

