package com.juanitos.data.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_38_39 = object : Migration(38, 39) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // savings_goals: one standing goal config per account (fixed € or % of income), plus a
        // dedupe watermark for the daily risk-notification check. No backfill: brand-new feature.
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `savings_goals` (
                `id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                `account_id` INTEGER NOT NULL,
                `goal_type` TEXT NOT NULL,
                `fixed_amount` REAL,
                `percentage` REAL,
                `notifications_enabled` INTEGER NOT NULL DEFAULT 0,
                `last_risk_notified_month` TEXT,
                `created_at` TEXT DEFAULT (datetime('now', 'localtime')),
                FOREIGN KEY(`account_id`) REFERENCES `accounts`(`id`) ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS `index_savings_goals_account_id` " +
                    "ON `savings_goals` (`account_id`)"
        )
    }
}
