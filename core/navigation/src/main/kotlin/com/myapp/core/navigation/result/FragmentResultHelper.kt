package com.myapp.core.navigation.result

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner

fun Fragment.setResult(requestKey: String, result: Bundle) {
    parentFragmentManager.setFragmentResult(requestKey, result)
}

fun Fragment.observeResult(
    requestKey: String,
    lifecycleOwner: LifecycleOwner,
    handler: (Bundle) -> Unit,
) {
    parentFragmentManager.setFragmentResultListener(requestKey, lifecycleOwner) { _, bundle ->
        handler(bundle)
    }
}
