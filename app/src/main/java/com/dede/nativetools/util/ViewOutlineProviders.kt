package com.dede.nativetools.util

import android.graphics.Outline
import android.view.View
import android.view.ViewOutlineProvider

class ViewOvalOutlineProvider(private val clip: Boolean = false) : ViewOutlineProvider() {

    override fun getOutline(view: View, outline: Outline) {
        view.clipToOutline = clip
        outline.setOval(0, 0, view.width, view.height)
    }
}

class ViewRoundRectOutlineProvider(private val radius: Float, private val clip: Boolean = false) :
    ViewOutlineProvider() {

    override fun getOutline(view: View, outline: Outline) {
        view.clipToOutline = clip
        outline.setRoundRect(0, 0, view.width, view.height, radius)
    }
}
