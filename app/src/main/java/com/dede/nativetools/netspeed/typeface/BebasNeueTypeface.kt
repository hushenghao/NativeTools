package com.dede.nativetools.netspeed.typeface

import android.content.Context
import android.graphics.Typeface

class BebasNeueTypeface(context: Context) : DownloadTypeface(context) {

    override val downloadUrl: String
        get() = "https://gitee.com/dede_hu/fonts/raw/master/BebasNeue.ttf"

    override val fontName: String
        get() = "BebasNeue.ttf"

    override fun appStyle(typeface: Typeface, style: Int): Typeface {
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