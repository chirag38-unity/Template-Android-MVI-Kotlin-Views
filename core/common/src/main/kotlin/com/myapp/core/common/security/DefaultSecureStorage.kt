package com.myapp.core.common.security

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import androidx.core.content.edit

/**
 * Default [SecureStorage] implementation backed by [SharedPreferences].
 * For production use, replace with [androidx.security.crypto.EncryptedSharedPreferences]
 * by adding `androidx.security:security-crypto` to the dependencies.
 */
class DefaultSecureStorage @Inject constructor(
    @ApplicationContext context: Context,
) : SecureStorage {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("secure_prefs", Context.MODE_PRIVATE)

    override fun putString(key: String, value: String) {
        prefs.edit { putString(key, value) }
    }

    override fun getString(key: String): String? = prefs.getString(key, null)

    override fun putBoolean(key: String, value: Boolean) {
        prefs.edit { putBoolean(key, value) }
    }

    override fun getBoolean(key: String, default: Boolean): Boolean =
        prefs.getBoolean(key, default)

    override fun remove(key: String) {
        prefs.edit { remove(key) }
    }

    override fun clear() {
        prefs.edit { clear() }
    }
}
