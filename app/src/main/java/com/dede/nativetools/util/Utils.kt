package com.dede.nativetools.util

import android.app.ActivityManager
import android.app.Service
import android.content.Context
import java.lang.NumberFormatException


fun String?.safeInt(default: Int): Int {
    if (this == null) return default
    return try {
        this.toInt()
    } catch (e: NumberFormatException) {
        default
    }
}

object Utils {

    fun isServiceRunning(service: Class<Service>, context: Context): Boolean {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val infoList = am.getRunningServices(0x7FFFFFFF)
        for (info in infoList) {
            if (service.name == info.service.className) {
                return true
            }
        }
        return false
    }
}