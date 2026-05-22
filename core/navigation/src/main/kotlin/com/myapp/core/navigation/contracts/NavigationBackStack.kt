package com.myapp.core.navigation.contracts

import androidx.fragment.app.Fragment

/**
 * Contract for managing a manual fragment back-stack within a single tab container.
 *
 * Implemented by [com.myapp.core.navigation.controller.FragmentBackStackController].
 *
 * Abstracting this interface allows future replacement with Compose navigation,
 * dialog navigation, or custom animations without changing the surrounding container logic.
 */
interface NavigationBackStack {
    /**
     * Push [fragment] on top of the current stack using the optional [tag].
     */
    fun push(fragment: Fragment, tag: String? = null)

    /**
     * Pop the top-most fragment.
     *
     * @return `true` if a pop was initiated; `false` if the stack was already empty.
     */
    fun pop(): Boolean

    /** `true` when at least one fragment is stacked above the root destination. */
    val hasBackStack: Boolean
}
