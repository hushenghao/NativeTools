package com.dede.nativetools.netspeed.typeface

import android.content.Context
import android.graphics.Typeface
import android.util.ArrayMap
import com.dede.nativetools.R
import com.dede.nativetools.util.globalContext

interface TypefaceGetter {

    companion object {

        const val FONT_NORMAL = "Normal"
        const val FONT_DEBUG = "Debug"

        private val caches = ArrayMap<String, TypefaceGetter>()

        private lateinit var fontList: List<String>

        fun create(context: Context, key: String): TypefaceGetter {
            var getter = caches[key]
            if (getter != null) {
                return getter
            }
            val appContext = context.applicationContext
            getter =
                when (key) {
                    FONT_NORMAL -> NormalTypeface()
                    FONT_DEBUG -> DebugTypeface(appContext)
                    else -> {
                        if (!::fontList.isInitialized) {
                            val fontArr =
                                context.resources.getStringArray(R.array.net_speed_font_value)
                            fontList = fontArr.toList()
                        }
                        if (fontList.contains(key)) {
                            DownloadTypefaceImpl(appContext, "$key.ttf")
                        } else {
                            NormalTypeface()
                            //throw IllegalStateException("Unknown font name: $key")
                        }
                    }
                }
            caches[key] = getter
            return getter
        }

        fun getOrDefault(key: String, style: Int): Typeface {
            return create(globalContext, key).get(style)
        }

        fun applyStyle(typeface: Typeface, style: Int): Typeface {
            return when (style) {
                Typeface.BOLD -> {
                    Typeface.create(typeface, Typeface.BOLD)
                }
                //                Typeface.ITALIC -> {
                //                    Typeface.create(typeface, Typeface.ITALIC)
                //                }
                //                Typeface.BOLD_ITALIC -> {
                //                    Typeface.create(typeface, Typeface.BOLD_ITALIC)
                //                }
                else -> {
                    typeface
                }
            }
        }
    }

    fun get(style: Int): Typeface

    fun canApply(): Boolean {
        return true
    }
}
