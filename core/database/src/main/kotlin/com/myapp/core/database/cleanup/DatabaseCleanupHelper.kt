package com.myapp.core.database.cleanup

import androidx.room.RoomDatabase
import androidx.room.withTransaction
import javax.inject.Inject

interface DatabaseCleanupHelper {
    suspend fun clearAll()
}

class DefaultDatabaseCleanupHelper @Inject constructor(
    private val db: RoomDatabase,
) : DatabaseCleanupHelper {

    override suspend fun clearAll() {
        db.withTransaction {
            db.clearAllTables()
        }
    }
}
