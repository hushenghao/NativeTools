package com.dede.nativetools.netspeed.typeface

import android.content.Context
import android.graphics.Typeface

class DebugTypeface(context: Context) : TypefaceGetter {

    val fontName = "ZCOOLQingKeHuangYou.ttf"

    private val typeface by lazy {
        kotlin.runCatching {
            Typeface.createFromAsset(context.assets, fontName)
        }.getOrDefault(Typeface.DEFAULT)
    }

    override fun get(style: Int): Typeface {
        return TypefaceGetter.applyStyle(typeface, style)
    }
}