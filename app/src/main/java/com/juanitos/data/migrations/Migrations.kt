package com.juanitos.data.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add the new column to the table
        database.execSQL("alter table batch_foods add column grams_used integer default null")
    }
}

val MIGRATION_10_11 = object : Migration(10, 11) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // 1. Create temporary table with new schema
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS batch_food_ingredients_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                batch_food_id INTEGER NOT NULL,
                ingredient_id INTEGER NOT NULL,
                grams INTEGER NOT NULL,
                FOREIGN KEY(batch_food_id) REFERENCES batch_foods(id) ON DELETE CASCADE,
                FOREIGN KEY(ingredient_id) REFERENCES ingredients(id) ON DELETE CASCADE
            )
        """.trimIndent()
        )

        // 2. Copy data with type conversion
        database.execSQL(
            """
            INSERT INTO batch_food_ingredients_new 
            (id, batch_food_id, ingredient_id, grams)
            SELECT id, batch_food_id, ingredient_id, CAST(grams AS INTEGER)
            FROM batch_food_ingredients
        """.trimIndent()
        )

        // 3. Drop old table
        database.execSQL("DROP TABLE batch_food_ingredients")

        // 4. Rename new table
        database.execSQL("ALTER TABLE batch_food_ingredients_new RENAME TO batch_food_ingredients")
    }
}

val MIGRATION_11_12 = object : Migration(11, 12) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // 1. Create new table with correct schema
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS food_ingredients_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                food_id INTEGER NOT NULL,
                ingredient_id INTEGER,
                batch_food_id INTEGER,
                grams INTEGER NOT NULL,
                FOREIGN KEY (food_id) REFERENCES foods(id) ON DELETE CASCADE,
                FOREIGN KEY (ingredient_id) REFERENCES ingredients(id) ON DELETE CASCADE,
                FOREIGN KEY (batch_food_id) REFERENCES batch_foods(id) ON DELETE CASCADE
            )
        """.trimIndent()
        )

        // 2. Copy data with type conversion
        database.execSQL(
            """
            INSERT INTO food_ingredients_new 
            (id, food_id, ingredient_id, batch_food_id, grams)
            SELECT 
                id, 
                food_id, 
                ingredient_id, 
                batch_food_id, 
                CAST(grams AS INTEGER)
            FROM food_ingredients
        """.trimIndent()
        )

        // 3. Drop old table
        database.execSQL("DROP TABLE food_ingredients")

        // 4. Rename new table
        database.execSQL("ALTER TABLE food_ingredients_new RENAME TO food_ingredients")

        // 5. Optional: Recreate indexes if any existed
    }
}

val MIGRATION_12_13 = object : Migration(12, 13) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // 1. Create temporary table with new default
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS foods_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                created_at TEXT DEFAULT (date('now', 'localtime'))
            )
        """.trimIndent()
        )

        // 2. Copy existing data
        database.execSQL(
            """
            INSERT INTO foods_new (id, name, created_at)
            SELECT id, name, created_at FROM foods
        """.trimIndent()
        )

        // 3. Drop old table
        database.execSQL("DROP TABLE foods")

        // 4. Rename new table
        database.execSQL("ALTER TABLE foods_new RENAME TO foods")
    }
}

val MIGRATION_13_14 = object : Migration(13, 14) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // 1. Create temporary table with new default
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS foods_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                created_at TEXT DEFAULT (datetime('now', 'localtime'))
            )
        """.trimIndent()
        )

        // 2. Copy existing data
        database.execSQL(
            """
            INSERT INTO foods_new (id, name, created_at)
            SELECT id, name, created_at FROM foods
        """.trimIndent()
        )

        // 3. Drop old table
        database.execSQL("DROP TABLE foods")

        // 4. Rename new table
        database.execSQL("ALTER TABLE foods_new RENAME TO foods")
    }
}