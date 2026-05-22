package com.myapp.core.navigation

import androidx.fragment.app.Fragment

/**
 * Navigation contract for pushing and popping [Fragment] destinations within a single tab's
 * independent back-stack.
 *
 * Implemented by [com.myapp.root.RootContainerFragment] (one instance per tab), which owns
 * a [com.myapp.navigation.BackStackController] backed by a child [FragmentManager].
 *
 * Feature fragments obtain this by casting their immediate parent fragment:
 * ```kotlin
 * (parentFragment as? FragmentNavigator)?.push(someFragment)
 * ```
 *
 * ## Design rationale
 *
 * Keeping this interface lightweight and tab-scoped means features remain fully decoupled
 * from the app's root navigation hierarchy. A feature only needs to know *that* it can push
 * a fragment — not *where* the container lives or how tabs are managed.
 *
 * For launching a new Activity from a feature see [ActivityNavigator].
 */
interface FragmentNavigator {
    /**
     * Push [fragment] onto the current tab's back-stack.
     *
     * The host container replaces the current destination and records an entry in its
     * back-stack so that [pop] can return to the previous screen.
     */
    fun push(fragment: Fragment)

    /**
     * Pop the top fragment from the current tab's back-stack.
     *
     * @return `true` if a fragment was popped; `false` if the back-stack was empty (i.e.
     *         the root destination is already shown).
     */
    fun pop(): Boolean
}
