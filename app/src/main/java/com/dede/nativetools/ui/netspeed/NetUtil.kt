package com.dede.nativetools.ui.netspeed


import java.math.BigDecimal
import kotlin.math.roundToInt


/**
 * Created by hsh on 2017/5/15 015 下午 05:14.
 */
object NetUtil {

    private const val UNIT = 1024
    private const val THRESHOLD = 900

    fun formatNetSpeedStr(speedByte: Long): String {
        var speed = speedByte.toDouble()
        var suffix = "B/s"
        if (speed > THRESHOLD) {
            speed /= UNIT
            suffix = "Kb/s"
        }
        if (speed > THRESHOLD) {
            speed /= UNIT
            suffix = "Mb/s"
        }
        if (speed > THRESHOLD) {
            speed /= UNIT
            suffix = "Gb/s"
        }
        if (speed > THRESHOLD) {
            speed /= UNIT
            suffix = "Tb/s"
        }
        if (speed > THRESHOLD) {
            speed /= UNIT
            suffix = "Pb/s"
        }
        val speedStr = BigDecimal(speed)
            .setScale(2, BigDecimal.ROUND_HALF_UP)
            .stripTrailingZeros()
            .toPlainString()
        return speedStr + suffix
    }

    fun formatNetSpeed(speedByte: Long): Array<String> {
        var speed = speedByte.toDouble()
        var suffix = "B/s"
        if (speed > THRESHOLD) {
            speed /= UNIT
            suffix = "Kb/s"
        }
        if (speed > THRESHOLD) {
            speed /= UNIT
            suffix = "Mb/s"
        }
        if (speed > THRESHOLD) {
            speed /= UNIT
            suffix = "Gb/s"
        }
        if (speed > THRESHOLD) {
            speed /= UNIT
            suffix = "Tb/s"
        }
        if (speed > THRESHOLD) {
            speed /= UNIT
            suffix = "Pb/s"
        }
        val format = when {
            speed >= 100 -> { // 100.2 -> 100
                speed.roundToInt().toString()
            }
            speed >= 10 -> {// 10.22 -> 10.2
                BigDecimal(speed)
                    .setScale(1, BigDecimal.ROUND_HALF_UP)
                    .stripTrailingZeros()
                    .toPlainString()
            }
            else -> {// 0.223 -> 0.22
                BigDecimal(speed)
                    .setScale(2, BigDecimal.ROUND_HALF_UP)
                    .stripTrailingZeros()
                    .toPlainString()
            }
        }
        return arrayOf(format, suffix)
    }

    fun formatNetSize(speedByte: Long): String {
        var speed = speedByte.toDouble()
        var suffix = "B"
        if (speed > THRESHOLD) {
            speed /= UNIT
            suffix = "K"
        }
        if (speed > THRESHOLD) {
            speed /= UNIT
            suffix = "M"
        }
        if (speed > THRESHOLD) {
            speed /= UNIT
            suffix = "G"
        }
        if (speed > THRESHOLD) {
            speed /= UNIT
            suffix = "T"
        }
        if (speed > THRESHOLD) {
            speed /= UNIT
            suffix = "P"
        }
        val format = when {
            speed >= 10 -> { // 10.2
                speed.roundToInt().toString()
            }
            else -> {// 1.2
                BigDecimal(speed)
                    .setScale(1, BigDecimal.ROUND_HALF_UP)
                    .stripTrailingZeros()
                    .toPlainString()
            }
        }
        return format + suffix
    }
}
