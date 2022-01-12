@file:JvmName("HandlerKt")

package com.dede.nativetools.util

import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.core.os.ExecutorCompat
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.plus

val uiHandler by lazy { Handler(Looper.getMainLooper()) }

fun Handler.singlePost(r: Runnable, delayMillis: Long = 0) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        if (this.hasCallbacks(r)) {
            this.removeCallbacks(r)
        }
    } else {
        this.removeCallbacks(r)
    }
    this.postDelayed(r, delayMillis)
}

val uiExecutor by lazy { ExecutorCompat.create(uiHandler) }

val mainScope by lazy {
    val exceptionHandler = CoroutineExceptionHandler { _, e ->
        e.printStackTrace()
    }
    MainScope() + exceptionHandler
}
