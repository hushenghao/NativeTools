package com.dede.nativetools

import android.content.Context
import android.util.Log
import androidx.startup.Initializer
import me.weishu.reflection.Reflection

/**
 * Reflection unseal
 */
class ReflectionInitializer : Initializer<ReflectionInitializer> {

    override fun create(context: Context) = apply {
        val unseal = Reflection.unseal(context)
        Log.i("ReflectionInitializer", "unseal: $unseal")
    }

    override fun dependencies() = emptyList<Class<out Initializer<*>>>()
}