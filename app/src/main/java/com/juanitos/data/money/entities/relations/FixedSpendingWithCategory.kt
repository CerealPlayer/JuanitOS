package com.juanitos.data.money.entities.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.juanitos.data.money.entities.Category
import com.juanitos.data.money.entities.FixedSpending

data class FixedSpendingWithCategory(
    @Embedded
    val fixedSpending: FixedSpending,
    @Relation(
        entity = Category::class,
        parentColumn = "category_id",
        entityColumn = "id"
    )
    val category: Category
)
