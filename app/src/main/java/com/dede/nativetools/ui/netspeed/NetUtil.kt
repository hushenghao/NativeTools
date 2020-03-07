package com.dede.nativetools.ui.netspeed


import java.math.BigDecimal
import kotlin.math.roundToInt


/**
 * Created by hsh on 2017/5/15 015 下午 05:14.
 */
object NetUtil {

    fun formatNetSpeed(downloadSpeed: Long): Array<String> {
        var speed = downloadSpeed.toDouble()
        var unit = "B/s"
        if (speed > 999) {
            speed /= 1024.0
            unit = "Kb/s"
        }
        if (speed > 999) {
            speed /= 1024.0
            unit = "Mb/s"
        }
        if (speed > 999) {
            speed /= 1024.0
            unit = "Gb/s"
        }
        if (speed > 999) {
            speed /= 1024.0
            unit = "Tb/s"
        }
        if (speed > 999) {
            speed /= 1024.0
            unit = "Pb/s"
        }
        val format = when {
            speed >= 100 -> { //100.2
                speed.roundToInt().toString()
            }
            speed >= 10 -> {//10.22
                BigDecimal(speed)
                    .setScale(1, BigDecimal.ROUND_HALF_UP)
                    .stripTrailingZeros()
                    .toPlainString()
            }
            else -> {// 1.22
                BigDecimal(speed)
                    .setScale(2, BigDecimal.ROUND_HALF_UP)
                    .stripTrailingZeros()
                    .toPlainString()
            }
        }
        return arrayOf(format, unit)
    }

    fun formatNetSize(size: Long): String {
        var speed = size.toDouble()
        var unit = "b"
        if (speed > 999) {
            speed /= 1024.0
            unit = "k"
        }
        if (speed > 999) {
            speed /= 1024.0
            unit = "m"
        }
        if (speed > 999) {
            speed /= 1024.0
            unit = "g"
        }
        if (speed > 999) {
            speed /= 1024.0
            unit = "t"
        }
        if (speed > 999) {
            speed /= 1024.0
            unit = "p"
        }
        val format = when {
            speed >= 10 -> { //10.2
                speed.roundToInt().toString()
            }
            else -> {// 1.2
                BigDecimal(speed)
                    .setScale(1, BigDecimal.ROUND_HALF_UP)
                    .stripTrailingZeros()
                    .toPlainString()
            }
        }
        return format + unit
    }
}
