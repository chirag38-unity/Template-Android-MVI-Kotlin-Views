package com.myapp.core.database.transaction

import androidx.room.RoomDatabase
import androidx.room.withTransaction
import javax.inject.Inject

class TransactionHelper @Inject constructor(
    private val db: RoomDatabase,
) {
    suspend fun <T> inTransaction(block: suspend () -> T): T = db.withTransaction { block() }
}
