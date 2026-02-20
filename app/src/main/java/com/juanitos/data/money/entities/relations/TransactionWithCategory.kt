package com.juanitos.data.money.entities.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.juanitos.data.money.entities.Category
import com.juanitos.data.money.entities.Transaction

data class TransactionWithCategory(
    @Embedded
    val transaction: Transaction,
    @Relation(
        entity = Category::class,
        parentColumn = "category_id",
        entityColumn = "id"
    )
    val category: Category?
)
