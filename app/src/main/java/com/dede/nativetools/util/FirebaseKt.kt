@file:JvmName("FirebaseKt")

package com.dede.nativetools.util

import com.google.firebase.ktx.Firebase
import com.google.firebase.perf.ktx.performance
import com.google.firebase.perf.ktx.trace

inline fun <T> track(name: String, block: () -> T): T {
    return Firebase.performance.newTrace(name).trace { block() }
}