package com.dede.nativetools.ui.netspeed


import java.text.NumberFormat


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
        val numberFormat = NumberFormat.getInstance()
        numberFormat.maximumFractionDigits = 1 //小数点一位
        var format = numberFormat.format(speed)
        val length = format.length
        if (length >= 4) { //100.2
            format = format.substring(0, length - 2)
        }
        return arrayOf(format, unit)
    }
}
