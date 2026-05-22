package com.myapp.core.ui.dialogs

import android.app.AlertDialog
import android.content.Context
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object DialogHelper {

    fun showAlert(
        context: Context,
        title: String,
        message: String,
        positiveText: String = "OK",
        positiveAction: () -> Unit = {},
        negativeText: String? = null,
        negativeAction: (() -> Unit)? = null,
    ): AlertDialog {
        return AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(positiveText) { _, _ -> positiveAction() }
            .apply {
                if (negativeText != null) {
                    setNegativeButton(negativeText) { _, _ -> negativeAction?.invoke() }
                }
            }
            .create()
            .also { it.show() }
    }

    suspend fun showConfirm(
        context: Context,
        title: String,
        message: String,
    ): Boolean = suspendCancellableCoroutine { cont ->
        val dialog = AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Confirm") { _, _ -> cont.resume(true) }
            .setNegativeButton("Cancel") { _, _ -> cont.resume(false) }
            .setOnCancelListener { cont.resume(false) }
            .create()
        cont.invokeOnCancellation { dialog.dismiss() }
        dialog.show()
    }

    fun showLoading(context: Context): AlertDialog {
        return AlertDialog.Builder(context)
            .setMessage("Loading…")
            .setCancelable(false)
            .create()
            .also { it.show() }
    }
}
