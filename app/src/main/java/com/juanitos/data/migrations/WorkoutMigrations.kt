package com.juanitos.data.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_19_20 = object : Migration(19, 20) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS exercise_definitions (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                description TEXT,
                type TEXT NOT NULL,
                created_at TEXT DEFAULT (datetime('now', 'localtime'))
            )
            """.trimIndent()
        )
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS workouts (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                date TEXT NOT NULL,
                notes TEXT,
                created_at TEXT DEFAULT (datetime('now', 'localtime'))
            )
            """.trimIndent()
        )
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS workout_exercises (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                workout_id INTEGER NOT NULL,
                exercise_definition_id INTEGER NOT NULL,
                position INTEGER NOT NULL DEFAULT 0,
                FOREIGN KEY(workout_id) REFERENCES workouts(id) ON DELETE CASCADE,
                FOREIGN KEY(exercise_definition_id) REFERENCES exercise_definitions(id) ON DELETE RESTRICT
            )
            """.trimIndent()
        )
        database.execSQL("CREATE INDEX IF NOT EXISTS index_workout_exercises_workout_id ON workout_exercises(workout_id)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_workout_exercises_exercise_definition_id ON workout_exercises(exercise_definition_id)")
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS workout_sets (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                workout_exercise_id INTEGER NOT NULL,
                reps INTEGER,
                duration_seconds INTEGER,
                weight_kg REAL,
                position INTEGER NOT NULL DEFAULT 0,
                FOREIGN KEY(workout_exercise_id) REFERENCES workout_exercises(id) ON DELETE CASCADE
            )
            """.trimIndent()
        )
        database.execSQL("CREATE INDEX IF NOT EXISTS index_workout_sets_workout_exercise_id ON workout_sets(workout_exercise_id)")
    }
}

val MIGRATION_20_21 = object : Migration(20, 21) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE workouts ADD COLUMN start_time TEXT")
        database.execSQL("ALTER TABLE workouts ADD COLUMN end_time TEXT")
    }
}
