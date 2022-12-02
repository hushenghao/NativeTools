package com.dede.nativetools.netspeed.typeface

import android.graphics.Typeface

class NormalTypeface : TypefaceGetter {

    override fun get(style: Int): Typeface {
        return when (style) {
            Typeface.BOLD -> {
                Typeface.defaultFromStyle(Typeface.BOLD)
            }
            // Typeface.ITALIC -> {
            //    Typeface.defaultFromStyle(Typeface.ITALIC)
            // }
            // Typeface.BOLD_ITALIC -> {
            //    Typeface.defaultFromStyle(Typeface.BOLD_ITALIC)
            // }
            else -> {
                Typeface.defaultFromStyle(Typeface.NORMAL)
            }
        }
    }
}
