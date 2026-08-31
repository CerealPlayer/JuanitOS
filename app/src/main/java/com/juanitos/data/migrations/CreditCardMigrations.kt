package com.juanitos.data.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.juanitos.data.money.CREDIT_CARD_PAYMENT_CATEGORY_NAME

val MIGRATION_35_36 = object : Migration(35, 36) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `credit_cards` (
                `id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                `account_id` INTEGER NOT NULL,
                `name` TEXT NOT NULL,
                `payment_day` INTEGER NOT NULL,
                `last_settled_at` TEXT,
                `created_at` TEXT DEFAULT (datetime('now', 'localtime')),
                FOREIGN KEY(`account_id`) REFERENCES `accounts`(`id`) ON DELETE CASCADE
            )
            """.trimIndent()
        )
        // credit_card_id is a foreign key, and SQLite's ALTER TABLE ADD COLUMN cannot add FK
        // constraints, so the transactions table must be recreated (Room validates FKs from
        // PRAGMA foreign_key_list, not just column presence).
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `transactions_new` (
                `id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                `account_id` INTEGER NOT NULL,
                `amount` REAL NOT NULL,
                `category_id` INTEGER NOT NULL,
                `description` TEXT,
                `created_at` TEXT DEFAULT (datetime('now', 'localtime')),
                `frequency` TEXT,
                `recurrence_root_id` INTEGER,
                `credit_card_id` INTEGER,
                FOREIGN KEY(`account_id`) REFERENCES `accounts`(`id`) ON DELETE CASCADE,
                FOREIGN KEY(`category_id`) REFERENCES `categories`(`id`) ON DELETE CASCADE,
                FOREIGN KEY(`credit_card_id`) REFERENCES `credit_cards`(`id`) ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO transactions_new
                (id, account_id, amount, category_id, description, created_at, frequency, recurrence_root_id, credit_card_id)
            SELECT id, account_id, amount, category_id, description, created_at, frequency, recurrence_root_id, NULL
            FROM transactions
            """.trimIndent()
        )
        db.execSQL("DROP TABLE transactions")
        db.execSQL("ALTER TABLE transactions_new RENAME TO transactions")
        db.execSQL(
            "INSERT INTO categories (name, description) SELECT ?, ? WHERE NOT EXISTS " +
                    "(SELECT 1 FROM categories WHERE name = ?)",
            arrayOf(
                CREDIT_CARD_PAYMENT_CATEGORY_NAME,
                "Lump-sum credit card settlement",
                CREDIT_CARD_PAYMENT_CATEGORY_NAME
            )
        )
    }
}
