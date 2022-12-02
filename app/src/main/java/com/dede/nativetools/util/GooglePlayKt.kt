@file:JvmName("GooglePlayKt")

package com.dede.nativetools.util

import android.content.Context
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase

fun Context.isGooglePlayServicesAvailable(): Boolean {
    return try {
        GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this) ==
            ConnectionResult.SUCCESS
    } catch (e: Exception) {
        Firebase.crashlytics.recordException(e)
        false
    }
}
