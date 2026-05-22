package com.myapp.core.navigation.safe

import androidx.fragment.app.Fragment
import com.myapp.core.common.logging.Logger
import com.myapp.core.navigation.command.NavigationCommand
import com.myapp.core.navigation.contracts.RootNavigator

fun Fragment.safeNavigate(command: NavigationCommand, navigator: RootNavigator) {
    if (!isAdded || isDetached || activity == null) {
        Logger.logW({"safeNavigate: Fragment is not attached, ignoring command: $command"})
        return
    }
    try {
        when (command) {
            is NavigationCommand.OpenTab -> navigator.navigateTo(command.tabId)
            is NavigationCommand.NavigateBack -> activity?.onBackPressedDispatcher?.onBackPressed()
            else -> Logger.logW({"safeNavigate: Unhandled navigation command: $command"})
        }
    } catch (e: IllegalStateException) {
        Logger.logE({"safeNavigate: Navigation failed: $command"}, throwableBuilder = { e })
    }
}
