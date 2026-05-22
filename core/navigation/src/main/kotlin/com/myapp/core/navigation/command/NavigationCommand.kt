package com.myapp.core.navigation.command

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment

sealed interface NavigationCommand {
    data class NavigateTo(val route: String, val args: Bundle? = null) : NavigationCommand
    data object NavigateBack : NavigationCommand
    data class NavigateBackTo(
        val fragmentClass: Class<out Fragment>,
        val inclusive: Boolean = false,
    ) : NavigationCommand
    data class OpenTab(val tabId: String) : NavigationCommand
    data class OpenDeepLink(val uri: Uri) : NavigationCommand
    data class ShowBottomSheet(val fragment: Fragment) : NavigationCommand
    data object ClearBackStack : NavigationCommand
}
