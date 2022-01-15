package com.dede.nativetools.main

import android.graphics.Rect
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlin.math.hypot
import kotlin.math.min

class MainViewModel : ViewModel() {

    private val circularReveal = MutableLiveData<CircularReveal?>(null)

    fun setCircularReveal(decorView: View, rect: Rect) {
        circularReveal.value = createCircularReveal(decorView, rect)
    }

    private fun createCircularReveal(decorView: View, rect: Rect): CircularReveal {
        return CircularReveal(
            rect.left + rect.width() / 2,
            rect.top + rect.height() / 2,
            min(rect.width() / 2f, rect.height() / 2f),
            // 勾股定理，这次真的跟它有关系
            hypot(decorView.width.toFloat(), decorView.height.toFloat())
        )
    }

    fun getCircularRevealAndClean(): CircularReveal? {
        val value = circularReveal.value
        circularReveal.value = null
        return value
    }

}