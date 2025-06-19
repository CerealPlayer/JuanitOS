package com.juanitos.data.money.entities.relations

import com.juanitos.data.money.entities.Cycle
import com.juanitos.data.money.entities.FixedSpending
import com.juanitos.data.money.entities.Transaction

data class CurrentCycleWithDetails(
    val cycle: Cycle,
    val transactions: List<Transaction>,
    val fixedSpendings: List<FixedSpending>
)
