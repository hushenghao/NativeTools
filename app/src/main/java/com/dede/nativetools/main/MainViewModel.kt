package com.dede.nativetools.main

import android.graphics.Point
import android.os.Parcelable
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dede.nativetools.util.dpf
import kotlin.math.hypot
import kotlinx.parcelize.Parcelize

@Parcelize
data class CircularReveal(
    val centerX: Int,
    val centerY: Int,
    val startRadius: Float,
    val endRadius: Float,
) : Parcelable

class MainViewModel : ViewModel() {

    private val circularReveal = MutableLiveData<CircularReveal?>(null)

    fun setCircularReveal(decorView: View, point: Point) {
        circularReveal.value = createCircularReveal(decorView, point)
    }

    private fun createCircularReveal(decorView: View, point: Point): CircularReveal {
        return CircularReveal(
            point.x,
            point.y,
            20.dpf,
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
