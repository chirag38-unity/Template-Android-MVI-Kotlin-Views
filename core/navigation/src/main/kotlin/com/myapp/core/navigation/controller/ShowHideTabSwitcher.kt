package com.myapp.core.navigation.controller

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.myapp.core.navigation.contracts.TabSwitcher

/**
 * Default [TabSwitcher] strategy using Android's show/hide fragment transactions.
 *
 * Showing/hiding preserves each tab's [Fragment] instance (and its child
 * [FragmentManager]) across switches, so ViewModels, RecyclerView scroll positions,
 * and per-tab back-stacks are fully retained without re-creation.
 *
 * Alternative strategies (detach/attach, replace, custom animations) can be provided
 * by implementing [TabSwitcher] and passing a different instance to
 * [com.myapp.core.navigation.ui.NavigationHostFragment].
 */
class ShowHideTabSwitcher : TabSwitcher {

    override fun switch(
        fragmentManager: FragmentManager,
        containerId: Int,
        currentTag: String?,
        targetTag: String,
        factory: () -> Fragment,
    ) {
        fragmentManager.beginTransaction()
            .setReorderingAllowed(true)
            .apply {
                if (currentTag != null) {
                    fragmentManager.findFragmentByTag(currentTag)?.let { hide(it) }
                }
                val existing = fragmentManager.findFragmentByTag(targetTag)
                if (existing != null) {
                    show(existing)
                } else {
                    add(containerId, factory(), targetTag)
                }
            }
            .commitNow()
    }
}
