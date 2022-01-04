package com.dede.nativetools.netspeed.typeface

import android.content.Context
import android.graphics.Typeface
import android.util.ArrayMap
import com.dede.nativetools.util.globalContext

interface TypefaceGetter {

    companion object {

        const val FONT_NORMAL = "normal"
        const val FONT_BEBAS_KAI = "bebaskai"
        const val FONT_OSWALD = "oswald"

        private val caches = ArrayMap<String, TypefaceGetter>()

        fun create(context: Context, key: String? = null): TypefaceGetter {
            var getter = caches[key]
            if (getter != null) {
                return getter
            }
            getter = when (key) {
                FONT_NORMAL -> NormalTypeface()
                FONT_BEBAS_KAI -> BebasKaiTypeface(context)
                FONT_OSWALD -> OswaldTypeface(context)
                else -> BebasKaiTypeface(context)
            }
            caches[key] = getter
            return getter
        }

        fun get(key: String? = null, style: Int): Typeface {
            return create(globalContext, key).get(style)
        }
    }

    fun get(style: Int): Typeface
}