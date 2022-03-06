@file:JvmName("FirebaseKt")
@file:Suppress("NOTHING_TO_INLINE")

package com.dede.nativetools.util

import com.google.firebase.analytics.ktx.ParametersBuilder
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import com.google.firebase.perf.ktx.performance
import com.google.firebase.perf.ktx.trace

inline fun <T> track(name: String, block: () -> T): T {
    return Firebase.performance.newTrace(name).trace { block() }
}

inline fun event(name: String, crossinline block: ParametersBuilder.() -> Unit) {
    Firebase.analytics.logEvent(name, block)
}

inline fun event(name: String) {
    Firebase.analytics.logEvent(name, null)
}