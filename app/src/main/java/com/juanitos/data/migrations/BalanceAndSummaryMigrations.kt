package com.juanitos.data.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_37_38 = object : Migration(37, 38) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // current_balance: a cached running balance, maintained incrementally at the app level
        // instead of re-aggregating every transaction on every read.
        db.execSQL("ALTER TABLE accounts ADD COLUMN current_balance REAL NOT NULL DEFAULT 0.0")
        db.execSQL(
            "ALTER TABLE accounts ADD COLUMN last_balance_sweep_at TEXT DEFAULT (datetime('now', 'localtime'))"
        )

        // Backfill current_balance from existing transactions, using the same exclusion rules as
        // computeAccountSummary/isBalanceAffecting: skip pending (future-dated) and credit-card-
        // linked transactions.
        db.execSQL(
            """
            UPDATE accounts
            SET current_balance = starting_balance - (
                SELECT COALESCE(SUM(t.amount), 0)
                FROM transactions t
                WHERE t.account_id = accounts.id
                  AND t.credit_card_id IS NULL
                  AND t.created_at <= datetime('now', 'localtime')
            )
            """.trimIndent()
        )

        // monthly_summaries: per-account, per-month snapshots of income/expenses/net, kept live
        // for the current month and finalized once a month elapses, so stats/budget features can
        // read a single row instead of aggregating a month's transactions.
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `monthly_summaries` (
                `id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                `account_id` INTEGER NOT NULL,
                `month` TEXT NOT NULL,
                `total_income` REAL NOT NULL,
                `total_expenses` REAL NOT NULL,
                `net_balance` REAL NOT NULL,
                FOREIGN KEY(`account_id`) REFERENCES `accounts`(`id`) ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS `index_monthly_summaries_account_id_month` " +
                    "ON `monthly_summaries` (`account_id`, `month`)"
        )

        db.execSQL(
            """
            INSERT INTO monthly_summaries (account_id, month, total_income, total_expenses, net_balance)
            SELECT
                t.account_id,
                substr(t.created_at, 1, 7) AS month,
                COALESCE(SUM(CASE WHEN t.amount < 0 THEN -t.amount ELSE 0 END), 0) AS total_income,
                COALESCE(SUM(CASE WHEN t.amount > 0 THEN t.amount ELSE 0 END), 0) AS total_expenses,
                COALESCE(SUM(-t.amount), 0) AS net_balance
            FROM transactions t
            WHERE t.credit_card_id IS NULL
              AND t.created_at <= datetime('now', 'localtime')
            GROUP BY t.account_id, substr(t.created_at, 1, 7)
            """.trimIndent()
        )
    }
}
