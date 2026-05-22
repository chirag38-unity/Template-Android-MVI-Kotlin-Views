package com.myapp.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.myapp.core.database.dao.PlayerDao
import com.myapp.core.database.entity.PlayerEntity

@Database(
    entities = [PlayerEntity::class],
    version = 2,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun playerDao(): PlayerDao
}
