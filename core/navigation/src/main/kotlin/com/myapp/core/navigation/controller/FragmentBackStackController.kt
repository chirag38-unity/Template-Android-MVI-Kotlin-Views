package com.myapp.core.navigation.controller

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.myapp.core.navigation.contracts.NavigationBackStack

/**
 * Fragment-based implementation of [NavigationBackStack].
 *
 * Manages a manual fragment back-stack within a given container using
 * [FragmentManager] transactions, without relying on the Navigation Component.
 *
 * ## Transaction safety
 * [push] uses `commitAllowingStateLoss` to prevent [IllegalStateException] when
 * a transaction is triggered after the Activity or Fragment has saved its instance
 * state (e.g. from a fast network callback during orientation change).
 *
 * [pop] uses [FragmentManager.popBackStack] (async) rather than
 * `popBackStackImmediate` to avoid executing a synchronous operation from within
 * a callback where the manager's state may not yet be committed.
 */
class FragmentBackStackController(
    private val fragmentManager: FragmentManager,
    private val containerId: Int,
) : NavigationBackStack {

    override fun push(fragment: Fragment, tag: String?) {
        fragmentManager.beginTransaction()
            .setReorderingAllowed(true)
            .replace(containerId, fragment, tag)
            .addToBackStack(null)
            .commitAllowingStateLoss()
    }

    override fun pop(): Boolean {
        if (fragmentManager.backStackEntryCount == 0) return false
        fragmentManager.popBackStack()
        return true
    }

    override val hasBackStack: Boolean
        get() = fragmentManager.backStackEntryCount > 0
}
