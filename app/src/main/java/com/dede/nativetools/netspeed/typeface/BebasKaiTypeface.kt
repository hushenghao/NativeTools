package com.dede.nativetools.netspeed.typeface

import android.content.Context
import android.graphics.Typeface

class BebasKaiTypeface(context: Context) : TypefaceGetter {

    private val basic = Typeface.createFromAsset(context.assets, "BebasKai.ttf")

    override fun get(style: Int): Typeface {
        return when (style) {
            Typeface.BOLD -> {
                Typeface.create(basic, Typeface.BOLD)
            }
            Typeface.ITALIC -> {
                Typeface.create(basic, Typeface.ITALIC)
            }
            Typeface.BOLD_ITALIC -> {
                Typeface.create(basic, Typeface.BOLD_ITALIC)
            }
            else -> {
                basic
            }
        }
    }

}