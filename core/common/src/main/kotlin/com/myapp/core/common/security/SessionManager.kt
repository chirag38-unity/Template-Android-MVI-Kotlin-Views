package com.myapp.core.common.security

import javax.inject.Inject

private const val KEY_AUTH_TOKEN = "auth_token"
private const val KEY_REFRESH_TOKEN = "refresh_token"
private const val KEY_USER_ID = "user_id"

class SessionManager @Inject constructor(
    private val secureStorage: SecureStorage,
) {
    fun saveAuthToken(token: String) {
        secureStorage.putString(KEY_AUTH_TOKEN, token)
    }

    fun getAuthToken(): String? = secureStorage.getString(KEY_AUTH_TOKEN)

    fun saveRefreshToken(token: String) {
        secureStorage.putString(KEY_REFRESH_TOKEN, token)
    }

    fun getRefreshToken(): String? = secureStorage.getString(KEY_REFRESH_TOKEN)

    fun saveUserId(userId: String) {
        secureStorage.putString(KEY_USER_ID, userId)
    }

    fun getUserId(): String? = secureStorage.getString(KEY_USER_ID)

    fun clearSession() {
        secureStorage.remove(KEY_AUTH_TOKEN)
        secureStorage.remove(KEY_REFRESH_TOKEN)
        secureStorage.remove(KEY_USER_ID)
    }

    fun isLoggedIn(): Boolean = getAuthToken() != null
}
