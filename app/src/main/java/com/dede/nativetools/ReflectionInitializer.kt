package com.dede.nativetools

import android.content.Context
import android.util.Log
import androidx.startup.Initializer
import me.weishu.reflection.Reflection

/**
 * Reflection unseal
 */
class ReflectionInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        val result = Reflection.unseal(context)
        Log.i("ReflectionInitializer", "unseal: $result")
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }
}