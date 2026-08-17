package com.juanitos.data.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.juanitos.data.money.DEFAULT_CATEGORIES

val MIGRATION_29_30 = object : Migration(29, 30) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("alter table fixed_spendings add column day_of_month integer default null")
    }
}

val MIGRATION_30_31 = object : Migration(30, 31) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `income_schedules` (
                `id` INTEGER NOT NULL,
                `day_of_month` INTEGER NOT NULL,
                `amount` REAL NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )
    }
}

val MIGRATION_31_32 = object : Migration(31, 32) {
    override fun migrate(db: SupportSQLiteDatabase) {
        DEFAULT_CATEGORIES.forEach { category ->
            db.execSQL(
                "INSERT INTO categories (name, description) VALUES (?, ?)",
                arrayOf(category.name, category.description)
            )
        }
    }
}
