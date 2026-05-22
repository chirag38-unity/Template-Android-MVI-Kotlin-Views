package com.myapp.core.common.logging

import com.myapp.core.common.BuildConfig
import timber.log.Timber

/**
 * Project       : TemplateAndroidMVI
 * Author        : Chirag Redij
 * Created on    : Thursday, 21/05/26 at 16:43
 * -------------------------------------------------------------------------------------
 * Last updated  : chiragredij on Thursday, 21/05/26 at 16:43
 *
 * Description   : [Add a brief description of this file or component]
 *
 * Copyright (c) 2026 ChiragRedij. All rights reserved.
 */
object Logger {

    var loggingEnabled: Boolean = BuildConfig.DEBUG

    inline fun logD(
        messageBuilder: () -> String,
        tagBuilder: () -> String? = { null },
        throwableBuilder: () -> Throwable? = { null }
    ) {
        if (loggingEnabled) {
            val tag = tagBuilder()
            if (tag.isNullOrBlank()) {
                Timber.d(throwableBuilder(), messageBuilder())
            } else {
                Timber.tag(tag).d(throwableBuilder(), messageBuilder())
            }
        }
    }

    inline fun logI(
        messageBuilder: () -> String,
        tagBuilder: () -> String? = { null },
        throwableBuilder: () -> Throwable? = { null }
    ) {
        if (loggingEnabled) {
            val tag = tagBuilder()
            if (tag.isNullOrBlank()) {
                Timber.i(throwableBuilder(), messageBuilder())
            } else {
                Timber.tag(tag).i(throwableBuilder(), messageBuilder())
            }
        }
    }

    inline fun logW(
        messageBuilder: () -> String,
        tagBuilder: () -> String? = { null },
        throwableBuilder: () -> Throwable? = { null }
    ) {
        if (loggingEnabled) {
            val tag = tagBuilder()
            if (tag.isNullOrBlank()) {
                Timber.w(throwableBuilder(), messageBuilder())
            } else {
                Timber.tag(tag).w(throwableBuilder(), messageBuilder())
            }
        }
    }

    inline fun logE(
        messageBuilder: () -> String,
        tagBuilder: () -> String? = { null },
        throwableBuilder: () -> Throwable? = { null }
    ) {
        if (loggingEnabled) {
            val tag = tagBuilder()
            if (tag.isNullOrBlank()) {
                Timber.e(throwableBuilder(), messageBuilder())
            } else {
                Timber.tag(tag).e(throwableBuilder(), messageBuilder())
            }
        }
    }

}