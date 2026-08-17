package com.juanitos.data.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_28_29 = object : Migration(28, 29) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DROP TABLE IF EXISTS `workout_sets`")
        db.execSQL("DROP TABLE IF EXISTS `workout_exercises`")
        db.execSQL("DROP TABLE IF EXISTS `workouts`")
        db.execSQL("DROP TABLE IF EXISTS `exercise_definitions`")
        db.execSQL("DROP TABLE IF EXISTS `habit_entries`")
        db.execSQL("DROP TABLE IF EXISTS `habits`")
        db.execSQL("DROP TABLE IF EXISTS `climbing_boulder_attempts`")
        db.execSQL("DROP TABLE IF EXISTS `climbing_boulders`")
        db.execSQL("DROP TABLE IF EXISTS `climbing_media`")
        db.execSQL("DROP TABLE IF EXISTS `climbing_workouts`")
    }
}
