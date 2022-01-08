package com.dede.nativetools.netspeed.typeface

import android.content.Context
import android.graphics.Typeface
import java.io.File
import java.io.IOException

abstract class DownloadTypeface(val context: Context) : TypefaceGetter {

    companion object {

        fun create(context: Context, key: String): DownloadTypeface? {
            return TypefaceGetter.create(context, key) as? DownloadTypeface
        }

        private fun getFontDir(context: Context): File {
            return File(context.filesDir, "fonts").apply {
                if (!exists()) {
                    mkdirs()
                }
            }
        }

        fun getFontFile(context: Context, fontName: String): File {
            return File(getFontDir(context), fontName)
        }

        @Throws(IOException::class)
        fun loadFont(context: Context, fontName: String): Typeface {
            val fontFile = getFontFile(context, fontName)
            return Typeface.createFromFile(fontFile)
        }

        fun checkFont(context: Context, fontName: String): Boolean {
            val fontFile = getFontFile(context, fontName)
            if (!fontFile.exists()) return false
            val typeface = fontFile.runCatching(Typeface::createFromFile)
                .onFailure(Throwable::printStackTrace)
                .getOrNull()
            return typeface != null && typeface != Typeface.DEFAULT
        }
    }

    private val basic by lazy { loadFont(context, fontName) }

    override fun canApply(): Boolean {
        return checkFont(context, fontName)
    }

    abstract val downloadUrl: String

    abstract val fontName: String

    override fun get(style: Int): Typeface {
        if (!canApply()) {
            return TypefaceGetter.getOrDefault(TypefaceGetter.FONT_NORMAL, style)
        }
        return applyStyle(basic, style)
    }

    open fun applyStyle(typeface: Typeface, style: Int): Typeface {
        return TypefaceGetter.applyStyle(typeface, style)
    }
}

open class DownloadTypefaceImpl(context: Context, override val fontName: String) :
    DownloadTypeface(context) {
    override val downloadUrl: String
        get() = "https://gitee.com/dede_hu/fonts/raw/master/$fontName"
}
