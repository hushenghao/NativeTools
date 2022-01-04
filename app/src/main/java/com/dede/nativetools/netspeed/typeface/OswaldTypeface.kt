package com.dede.nativetools.netspeed.typeface

import android.content.Context
import android.graphics.Typeface
import android.os.Build
import androidx.annotation.RequiresApi

class OswaldTypeface(context: Context) : TypefaceGetter {

    private val basic = Typeface.createFromAsset(context.assets, "Oswald_wght.ttf")

    override fun get(style: Int): Typeface {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return apiP(style)
        }
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

    @RequiresApi(Build.VERSION_CODES.P)
    private fun apiP(style: Int): Typeface {
        return when (style) {
            Typeface.BOLD -> {
                Typeface.create(basic, 700, false)
            }
            Typeface.ITALIC -> {
                Typeface.create(basic, 400, true)
            }
            Typeface.BOLD_ITALIC -> {
                Typeface.create(basic, 700, true)
            }
            else -> {
                Typeface.create(basic, 400, false)
            }
        }
    }
}