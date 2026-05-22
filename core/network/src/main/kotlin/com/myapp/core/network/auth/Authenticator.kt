package com.myapp.core.network.auth

interface Authenticator {
    suspend fun getToken(): String?
    suspend fun refreshToken(): Boolean
    fun isAuthenticated(): Boolean
}
