package com.myapp.core.navigation.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.myapp.core.navigation.ActivityNavigator
import com.myapp.core.navigation.FragmentNavigator
import com.myapp.core.navigation.R
import com.myapp.core.navigation.controller.FragmentBackStackController
import com.myapp.core.navigation.databinding.FragmentNavigationContainerBinding

/**
 * Generic container fragment that owns one tab's independent fragment back-stack.
 *
 * [NavigationHostFragment] creates one [NavigationContainerFragment] per tab and uses
 * show/hide so ViewModels, scroll positions, and child fragment managers are fully
 * preserved across tab switches.
 *
 * Forward navigation within a tab: call [push].
 * Back navigation within a tab: call [pop].
 *
 * Feature fragments obtain the navigator by casting `parentFragment` to [FragmentNavigator]:
 * ```kotlin
 * (parentFragment as? FragmentNavigator)?.push(someFragment)
 * ```
 *
 * ## Back-stack safety
 * All fragment transactions are delegated to [FragmentBackStackController], which uses
 * `commitAllowingStateLoss` to prevent [IllegalStateException] when transactions are
 * triggered after the host has saved its instance state.
 */
class NavigationContainerFragment : Fragment(), FragmentNavigator, ActivityNavigator {

    private var _binding: FragmentNavigationContainerBinding? = null
    private val binding get() = _binding!!

    private lateinit var backStackController: FragmentBackStackController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentNavigationContainerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        backStackController = FragmentBackStackController(
            childFragmentManager,
            R.id.container_tab,
        )
    }

    /**
     * Place the root (entry) fragment for this tab.
     * No-op if the container already holds a fragment (i.e. after process restore).
     */
    fun setRootFragment(fragment: Fragment) {
        if (childFragmentManager.findFragmentById(R.id.container_tab) == null) {
            childFragmentManager.beginTransaction()
                .add(R.id.container_tab, fragment)
                .commitNow()
        }
    }

    // ── FragmentNavigator ──────────────────────────────────────────────────────

    override fun push(fragment: Fragment) {
        backStackController.push(fragment)
    }

    override fun pop(): Boolean = backStackController.pop()

    // ── ActivityNavigator ──────────────────────────────────────────────────────

    override fun launch(intent: Intent) {
        startActivity(intent)
    }

    /** `true` when there is at least one stacked fragment above the root. */
    val hasBackStack: Boolean
        get() = ::backStackController.isInitialized && backStackController.hasBackStack

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(): NavigationContainerFragment = NavigationContainerFragment()
    }
}
