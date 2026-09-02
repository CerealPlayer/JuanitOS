package com.juanitos.lib

import com.juanitos.data.money.entities.Transaction

/**
 * True if [transaction] should count toward an account's cached current_balance and monthly
 * summaries: not future-dated (pending) and not a credit-card-linked purchase (only the
 * auto-generated settlement transaction, which has creditCardId == null, counts).
 */
fun isBalanceAffecting(transaction: Transaction): Boolean =
    !isPendingTransaction(transaction.createdAt) && transaction.creditCardId == null
