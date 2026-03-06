package com.juanitos.data.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_23_24 = object : Migration(23, 24) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS climbing_workouts (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                date TEXT NOT NULL,
                start_time TEXT,
                end_time TEXT,
                notes TEXT,
                created_at TEXT DEFAULT (datetime('now', 'localtime'))
            )
            """.trimIndent()
        )
    }
}

val MIGRATION_24_25 = object : Migration(24, 25) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS climbing_media (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                file_path TEXT NOT NULL,
                mime_type TEXT NOT NULL,
                created_at TEXT DEFAULT (datetime('now', 'localtime'))
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS climbing_boulders (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                grade TEXT NOT NULL,
                style TEXT,
                picture_media_id INTEGER,
                created_at TEXT DEFAULT (datetime('now', 'localtime')),
                FOREIGN KEY(picture_media_id) REFERENCES climbing_media(id) ON DELETE SET NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS climbing_boulder_attempts (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                climbing_workout_id INTEGER NOT NULL,
                climbing_boulder_id INTEGER NOT NULL,
                video_media_id INTEGER,
                notes TEXT,
                created_at TEXT DEFAULT (datetime('now', 'localtime')),
                FOREIGN KEY(climbing_workout_id) REFERENCES climbing_workouts(id) ON DELETE CASCADE,
                FOREIGN KEY(climbing_boulder_id) REFERENCES climbing_boulders(id) ON DELETE RESTRICT,
                FOREIGN KEY(video_media_id) REFERENCES climbing_media(id) ON DELETE RESTRICT
            )
            """.trimIndent()
        )

        db.execSQL("CREATE INDEX IF NOT EXISTS index_climbing_boulders_picture_media_id ON climbing_boulders(picture_media_id)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_climbing_boulder_attempts_climbing_workout_id ON climbing_boulder_attempts(climbing_workout_id)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_climbing_boulder_attempts_climbing_boulder_id ON climbing_boulder_attempts(climbing_boulder_id)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_climbing_boulder_attempts_video_media_id ON climbing_boulder_attempts(video_media_id)")
    }
}

val MIGRATION_25_26 = object : Migration(25, 26) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS climbing_workouts_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                date TEXT NOT NULL,
                start_time TEXT,
                end_time TEXT,
                notes TEXT,
                created_at TEXT DEFAULT (datetime('now', 'localtime'))
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO climbing_workouts_new (id, date, start_time, end_time, notes, created_at)
            SELECT id, date, start_time, end_time, notes, created_at
            FROM climbing_workouts
            """.trimIndent()
        )
        db.execSQL("DROP TABLE climbing_workouts")
        db.execSQL("ALTER TABLE climbing_workouts_new RENAME TO climbing_workouts")
    }
}

val MIGRATION_26_27 = object : Migration(26, 27) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            ALTER TABLE climbing_boulder_attempts
            ADD COLUMN attempt_order INTEGER DEFAULT 0
            """.trimIndent()
        )
    }
}
