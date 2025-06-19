package com.juanitos.data.money.entities.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.juanitos.data.money.entities.Cycle
import com.juanitos.data.money.entities.FixedSpending
import com.juanitos.data.money.entities.Transaction

data class CurrentCycleWithDetails(
    @Embedded
    val cycle: Cycle,
    @Relation(
        entity = Transaction::class,
        parentColumn = "id",
        entityColumn = "cycle_id"
    )
    val transactions: List<Transaction>,
    @Relation(
        entity = FixedSpending::class,
        parentColumn = "id",
        entityColumn = "cycle_id"
    )
    val fixedSpendings: List<FixedSpending>
)
