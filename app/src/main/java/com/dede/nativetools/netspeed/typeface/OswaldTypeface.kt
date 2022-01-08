package com.dede.nativetools.netspeed.typeface

import android.content.Context
import android.graphics.Typeface
import android.os.Build
import androidx.annotation.RequiresApi

class OswaldTypeface(context: Context) : DownloadTypeface(context) {

    override val downloadUrl: String
        get() = "https://gitee.com/dede_hu/fonts/raw/master/Oswald_wght.ttf"

    override val fontName: String
        get() = "Oswald_wght.ttf"

    override fun appStyle(typeface: Typeface, style: Int): Typeface {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return apiP(typeface, style)
        }
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

    @RequiresApi(Build.VERSION_CODES.P)
    private fun apiP(typeface: Typeface, style: Int): Typeface {
        return when (style) {
            Typeface.BOLD -> {
                Typeface.create(typeface, 700, false)
            }
            Typeface.ITALIC -> {
                Typeface.create(typeface, 400, true)
            }
            Typeface.BOLD_ITALIC -> {
                Typeface.create(typeface, 700, true)
            }
            else -> {
                Typeface.create(typeface, 400, false)
            }
        }
    }
}