package com.myapp.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "players")
data class PlayerEntity(
    @PrimaryKey val id: String,
    val name: String,
    val nationality: String,
    val teamName: String,
    val photoUrl: String,
    val position: String,
    val goals: Int = 0,
    val assists: Int = 0,
    val rating: Double = 0.0,
    val description: String = "",
)
