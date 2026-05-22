package com.myapp.feature.feed.api.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Domain model representing a football player.
 * Parcelable so it can be passed between fragments via Bundle arguments.
 */
@Parcelize
data class Player(
    val id: String,
    val name: String,
    val nationality: String,
    val teamName: String,
    val photoUrl: String,
    val position: String,
    val goals: Int = 0,
    val assists: Int = 0,
    val rating: Double = 0.0,
    val description: String = "",
) : Parcelable
