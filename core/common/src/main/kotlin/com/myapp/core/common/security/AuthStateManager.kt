package com.myapp.core.common.security

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

sealed class AuthState {
    data object Authenticated : AuthState()
    data object Unauthenticated : AuthState()
}

@Singleton
class AuthStateManager @Inject constructor(
    private val sessionManager: SessionManager,
) {
    private val _authState = MutableStateFlow<AuthState>(resolveInitialState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private fun resolveInitialState(): AuthState =
        if (sessionManager.isLoggedIn()) AuthState.Authenticated else AuthState.Unauthenticated

    fun onLogin() {
        _authState.value = AuthState.Authenticated
    }

    fun onLogout() {
        sessionManager.clearSession()
        _authState.value = AuthState.Unauthenticated
    }

    fun isAuthenticated(): Boolean = _authState.value is AuthState.Authenticated
}
