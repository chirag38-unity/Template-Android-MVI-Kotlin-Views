package com.myapp.core.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

fun migration(
    from: Int,
    to: Int,
    block: SupportSQLiteDatabase.() -> Unit,
): Migration = object : Migration(from, to) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.block()
    }
}
