package com.dede.nativetools.netspeed.typeface

import android.content.Context
import android.graphics.Typeface
import android.util.ArrayMap
import com.dede.nativetools.util.globalContext

interface TypefaceGetter {

    companion object {

        const val FONT_NORMAL = "Normal"
        const val FONT_BEBAS_KAI = "BebasKai"
        const val FONT_BEBAS_NEUE = "BebasNeue"
        const val FONT_CREEPSTER = "Creepster"
        const val FONT_FJALLA_ONE = "FjallaOne"
        const val FONT_PIRATA_ONE = "PirataOne"
        const val FONT_PRESS_START_2P = "PressStart2P"
        const val FONT_CHAKRA_PETCH = "ChakraPetch"
        const val FONT_SQUADA_ONE = "SquadaOne"
        const val FONT_VT323 = "VT323"

        const val FONT_DEBUG = "Debug"

        private val caches = ArrayMap<String, TypefaceGetter>()

        fun create(context: Context, key: String): TypefaceGetter {
            var getter = caches[key]
            if (getter != null) {
                return getter
            }
            getter = when (key) {
                FONT_NORMAL -> NormalTypeface()
                FONT_BEBAS_KAI -> DownloadTypefaceImpl(context, "BebasKai.ttf")
                FONT_BEBAS_NEUE -> DownloadTypefaceImpl(context, "BebasNeue.ttf")
                FONT_CREEPSTER -> DownloadTypefaceImpl(context, "Creepster.ttf")
                FONT_FJALLA_ONE -> DownloadTypefaceImpl(context, "FjallaOne.ttf")
                FONT_PIRATA_ONE -> DownloadTypefaceImpl(context, "PirataOne.ttf")
                FONT_PRESS_START_2P -> DownloadTypefaceImpl(context, "PressStart2P.ttf")
                FONT_CHAKRA_PETCH -> DownloadTypefaceImpl(context, "ChakraPetch.ttf")
                FONT_SQUADA_ONE -> DownloadTypefaceImpl(context, "SquadaOne.ttf")
                FONT_VT323 -> DownloadTypefaceImpl(context, "VT323.ttf")
                //FONT_DEBUG -> DebugTypeface(context)
                else -> NormalTypeface()
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
                Typeface.ITALIC -> {
                    Typeface.create(typeface, Typeface.ITALIC)
                }
                Typeface.BOLD_ITALIC -> {
                    Typeface.create(typeface, Typeface.BOLD_ITALIC)
                }
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