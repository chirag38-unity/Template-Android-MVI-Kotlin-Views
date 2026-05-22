package com.myapp.feature.player.details.api

import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment

/**
 * Public entry-point of the player-details feature.
 *
 * Other feature `:impl` modules (feed, search) inject this interface via Hilt to navigate
 * to the player-details screen **without** a direct dependency on the `:impl` module.
 *
 * ## Two navigation paths
 *
 * | Caller       | Navigation mode       | Method to use           |
 * |--------------|-----------------------|-------------------------|
 * | FeedFragment | New Activity (full-screen) | [createActivityIntent] |
 * | SearchFragment | Push into current tab | [createFragment]        |
 *
 * The feature itself decides how a destination is constructed, what arguments it receives,
 * and how it handles its internal lifecycle. Callers only supply a lightweight [playerId].
 *
 * ## Why lightweight payloads?
 *
 * Passing a full domain model through a Bundle or Intent risks stale data (the object may
 * have been mutated since it was created), inflates Parcel sizes, and couples navigation to
 * the domain layer. A simple [String] ID sidesteps all of these problems: the destination
 * fragment/activity loads fresh data from the repository using the ID.
 *
 * ## Why no centralized destination registry?
 *
 * Centralising destinations (e.g. a sealed `AppDestination`) creates a god-object that every
 * feature must modify, causing merge conflicts and tight coupling across module boundaries.
 * Each feature owning its own contract keeps the module graph clean and scalable.
 */
interface PlayerDetailsFeatureEntry {

    /**
     * Create a new [PlayerDetailsFragment][com.myapp.feature.player.details.impl.presentation.PlayerDetailsFragment]
     * pre-configured with [playerId].
     *
     * Use this when the caller wants to push the details screen **within the current tab's
     * back-stack** (e.g. from SearchFragment).
     *
     * @param playerId Stable identifier of the player to display.
     */
    fun createFragment(playerId: String): Fragment

    /**
     * Build an [Intent] that launches
     * [PlayerDetailsActivity][com.myapp.feature.player.details.impl.presentation.PlayerDetailsActivity]
     * with [playerId] as its payload.
     *
     * Use this when the caller wants to open the details screen **as a new Activity**
     * (e.g. from FeedFragment, where details should appear on top of the entire app).
     *
     * @param context Android [Context] used to construct the [Intent].
     * @param playerId Stable identifier of the player to display.
     */
    fun createActivityIntent(context: Context, playerId: String): Intent
}
