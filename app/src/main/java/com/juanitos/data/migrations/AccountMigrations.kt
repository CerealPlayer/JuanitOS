package com.juanitos.data.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_33_34 = object : Migration(33, 34) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DROP TABLE IF EXISTS `transactions`")
        db.execSQL("DROP TABLE IF EXISTS `cycles`")
        db.execSQL("DROP TABLE IF EXISTS `income_schedules`")
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `accounts` (
                `id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                `name` TEXT NOT NULL,
                `starting_balance` REAL NOT NULL DEFAULT 0.0,
                `is_selected` INTEGER NOT NULL DEFAULT 0,
                `created_at` TEXT DEFAULT (datetime('now', 'localtime'))
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `transactions` (
                `id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                `account_id` INTEGER NOT NULL,
                `amount` REAL NOT NULL,
                `category_id` INTEGER NOT NULL,
                `description` TEXT,
                `created_at` TEXT DEFAULT (datetime('now', 'localtime')),
                FOREIGN KEY(`account_id`) REFERENCES `accounts`(`id`) ON DELETE CASCADE,
                FOREIGN KEY(`category_id`) REFERENCES `categories`(`id`) ON DELETE CASCADE
            )
            """.trimIndent()
        )
    }
}
