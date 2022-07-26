package com.dede.nativetools.netspeed.typeface

import android.graphics.Typeface

class NormalTypeface : TypefaceGetter {

    override fun get(style: Int): Typeface {
        return when (style) {
            Typeface.BOLD -> {
                Typeface.DEFAULT_BOLD
            }
            Typeface.ITALIC -> {
                Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            }
            Typeface.BOLD_ITALIC -> {
                Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC)
            }
            else -> {
                Typeface.DEFAULT
            }
        }
    }
}
