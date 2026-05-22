package com.myapp.core.navigation.deeplink

import android.net.Uri
import com.myapp.core.navigation.command.NavigationCommand

class DeepLinkParser {

    private val handlers = mutableListOf<DeepLinkHandler>()

    fun register(
        scheme: String,
        host: String,
        handler: (Uri) -> NavigationCommand?,
    ): DeepLinkParser {
        handlers.add(DeepLinkHandler(scheme, host, handler))
        return this
    }

    fun parse(uriString: String): NavigationCommand? {
        val uri = runCatching { Uri.parse(uriString) }.getOrNull() ?: return null
        return handlers
            .firstOrNull { it.scheme == uri.scheme && it.host == uri.host }
            ?.handler?.invoke(uri)
    }

    private data class DeepLinkHandler(
        val scheme: String,
        val host: String,
        val handler: (Uri) -> NavigationCommand?,
    )
}
