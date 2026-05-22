package com.myapp.core.navigation.contracts

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

/**
 * Strategy for switching between tab containers inside [com.myapp.core.navigation.ui.NavigationHostFragment].
 *
 * Separating this into an interface makes the switching strategy swappable:
 * - [com.myapp.core.navigation.controller.ShowHideTabSwitcher] — show/hide (default, preserves state)
 * - Future: detach/attach, replace, or custom animated variants
 */
interface TabSwitcher {
    /**
     * Switch the visible container from [currentTag] to [targetTag].
     *
     * If a fragment tagged [targetTag] does not yet exist in [fragmentManager],
     * [factory] is called to create it. Commits immediately so the caller can
     * rely on the updated fragment state.
     */
    fun switch(
        fragmentManager: FragmentManager,
        containerId: Int,
        currentTag: String?,
        targetTag: String,
        factory: () -> Fragment,
    )
}
