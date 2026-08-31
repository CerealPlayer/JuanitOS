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

/**
 * No-op at the SQL level: the `accounts` table created by [MIGRATION_33_34] already declares
 * `is_selected INTEGER NOT NULL DEFAULT 0`. This migration exists only to bump the DB version so
 * Room's schema identity check (now that [com.juanitos.data.money.entities.Account.isSelected]
 * declares `defaultValue = "0"` to match) passes for existing installs. Without this, a *fresh*
 * install would generate the accounts table straight from the entity annotations - which, before
 * this fix, had no SQL default for is_selected - and crash with a NOT NULL constraint violation
 * the first time [com.juanitos.data.money.daos.AccountDao.insert] (which omits is_selected,
 * relying on the default) ran.
 */
val MIGRATION_36_37 = object : Migration(36, 37) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Intentionally empty; see KDoc above.
    }
}
