package com.juanitos.data.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_34_35 = object : Migration(34, 35) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("alter table transactions add column frequency text default null")
        db.execSQL("alter table transactions add column recurrence_root_id integer default null")
        db.execSQL("DROP TABLE IF EXISTS `fixed_spendings`")
    }
}
