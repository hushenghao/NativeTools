package com.dede.nativetools.netspeed.typeface

import android.content.Context
import android.graphics.Typeface
import com.dede.nativetools.R

class DebugTypeface(context: Context) : TypefaceGetter {

    var fontName = "RobotoCondensed.ttf"
        private set

    private val typeface = kotlin.runCatching {
        Typeface.createFromAsset(context.assets, fontName)
    }.onFailure {
        val sysDef = context.getString(R.string.summary_default)
        fontName = "Debug Error(%s)".format(sysDef)
    }.getOrDefault(Typeface.DEFAULT)

    override fun get(style: Int): Typeface {
        return TypefaceGetter.applyStyle(typeface, style)
    }
}