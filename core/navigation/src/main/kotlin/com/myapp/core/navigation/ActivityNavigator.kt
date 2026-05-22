package com.myapp.core.navigation

import android.content.Intent

/**
 * Navigation contract for launching Android [Activity] destinations.
 *
 * Feature fragments that need to open a new Activity (e.g. `FeedFragment` opening
 * `PlayerDetailsActivity`) cast their host Activity to this interface and call [launch].
 *
 * This keeps feature fragments fully decoupled from concrete Activity classes — they
 * only know about the [Intent] produced by the target feature's own entry-point contract.
 *
 * ## Why not FragmentNavigator?
 *
 * [FragmentNavigator] is intentionally scoped to within-tab fragment navigation.
 * Launching a new Activity crosses that boundary (new task stack, separate lifecycle),
 * so a distinct interface is cleaner and avoids conflating the two navigation models.
 *
 * ## Usage example
 * ```kotlin
 * // Inside FeedFragment — no reference to PlayerDetailsActivity:
 * (activity as? ActivityNavigator)
 *     ?.launch(playerDetailsEntry.createActivityIntent(requireContext(), playerId))
 * ```
 *
 * Implemented by `com.myapp.root.RootFragment` (which delegates to `Activity.startActivity`).
 */
interface ActivityNavigator {
    /**
     * Launch the Activity described by [intent].
     *
     * Callers must not assume anything about the destination; they only supply an
     * [Intent] obtained from a feature-owned factory method.
     */
    fun launch(intent: Intent)
}
