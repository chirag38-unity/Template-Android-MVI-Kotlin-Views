package com.myapp.core.navigation.bundle

import android.os.Build
import android.os.Bundle
import android.os.Parcelable

inline fun <reified T : Parcelable> Bundle.putParcelableArg(key: String, value: T) {
    putParcelable(key, value)
}

@Suppress("DEPRECATION")
inline fun <reified T : Parcelable> Bundle.getParcelableArg(
    key: String,
): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelable(key, T::class.java)
    } else {
        getParcelable(key)
    }
}

fun Bundle.putStringArg(key: String, value: String) {
    putString(key, value)
}

fun Bundle.getStringArg(key: String): String? = getString(key)

fun Bundle.putIntArg(key: String, value: Int) {
    putInt(key, value)
}

fun Bundle.getIntArg(key: String, default: Int = 0): Int = getInt(key, default)

fun Bundle.putBooleanArg(key: String, value: Boolean) {
    putBoolean(key, value)
}

fun Bundle.getBooleanArg(key: String, default: Boolean = false): Boolean =
    getBoolean(key, default)
