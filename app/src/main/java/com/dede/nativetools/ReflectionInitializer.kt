package com.dede.nativetools

import android.content.Context
import android.util.Log
import androidx.startup.Initializer
import me.weishu.reflection.Reflection

/**
 * Reflection unseal
 */
class ReflectionInitializer : Initializer<Int> {

    override fun create(context: Context) = Reflection.unseal(context).apply {
        Log.i("ReflectionInitializer", "unseal: $this")
    }

    override fun dependencies() = emptyList<Class<out Initializer<*>>>()
}