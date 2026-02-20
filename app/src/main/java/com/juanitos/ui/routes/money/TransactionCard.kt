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
import com.juanitos.data.money.entities.relations.TransactionWithCategory
import com.juanitos.ui.commons.DeleteConfirmationDialog
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionCard(
    transactionWithCategory: TransactionWithCategory,
    onDelete: (TransactionWithCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    val transaction = transactionWithCategory.transaction
    val category = transactionWithCategory.category

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
            title = stringResource(R.string.confirm_delete_transaction),
            onConfirm = {
                onDelete(transactionWithCategory)
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
        val color = if (transaction.amount > 0) {
            MaterialTheme.colorScheme.error
        } else {
            MaterialTheme.colorScheme.primary
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = color
            )
        ) {
            Column(
                modifier = Modifier.padding(dimensionResource(R.dimen.padding_small))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = String.format(Locale.US, "%.2f€", transaction.amount))
                    Text(
                        text = category?.name ?: stringResource(R.string.uncategorized),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                if (transaction.description != null) {
                    Text(
                        text = transaction.description,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_small))
                    )
                }
            }
        }
    }
}



