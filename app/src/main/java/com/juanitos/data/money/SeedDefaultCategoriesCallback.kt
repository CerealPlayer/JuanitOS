package com.juanitos.data.money

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

object SeedDefaultCategoriesCallback : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        DEFAULT_CATEGORIES.forEach { category ->
            db.execSQL(
                "INSERT INTO categories (name, description) VALUES (?, ?)",
                arrayOf(category.name, category.description)
            )
        }
    }
}
