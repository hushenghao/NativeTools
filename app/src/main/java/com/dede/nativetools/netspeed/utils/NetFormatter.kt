package com.dede.nativetools.netspeed.utils


import androidx.annotation.IntDef
import java.text.DecimalFormat
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow


/**
 * 字节数格式化工具
 *
 * Created by hsh on 2017/5/15 015 下午 05:14.
 */
object NetFormatter {

    @IntDef(value = [
        FLAG_NULL,
        FLAG_FULL,
        FLAG_BYTE,
        FLAG_INFIX_SECOND
    ], flag = true)
    @Target(AnnotationTarget.VALUE_PARAMETER)
    annotation class NetFlag

    @IntDef(
        ACCURACY_EXACT,
        ACCURACY_SHORTER,
        ACCURACY_EQUAL_WIDTH,
        ACCURACY_EQUAL_WIDTH_EXACT
    )
    @Target(AnnotationTarget.VALUE_PARAMETER)
    annotation class Accuracy

    @IntDef(
        MIN_UNIT_BYTE,
        MIN_UNIT_KB,
        MIN_UNIT_MB
    )
    @Target(AnnotationTarget.VALUE_PARAMETER)
    annotation class MinUnit

    /**
     * 精确等宽格式
     *
     * 888
     * 88.8
     * 8.88
     */
    const val ACCURACY_EQUAL_WIDTH_EXACT = 1

    /**
     * 精确格式
     *
     * 88.88
     */
    const val ACCURACY_EXACT = 2

    /**
     * 低精度格式
     *
     * 88.8
     */
    const val ACCURACY_SHORTER = 4

    /**
     * 等宽格式
     *
     * 88
     * 8.8
     */
    const val ACCURACY_EQUAL_WIDTH = 3

    /**
     * 单位B字符的标志位
     *
     * Pair(8.8, B), Pair(8.8, KB)
     */
    const val FLAG_BYTE = 1

    /**
     * /s字符的标志位
     *
     * Pair(8.8, /s)
     */
    const val FLAG_INFIX_SECOND = 1 shl 1

    /**
     * 全量字符标志位
     *
     * Pair(8.8, KB/s)
     */
    const val FLAG_FULL = FLAG_BYTE or FLAG_INFIX_SECOND

    /**
     * 无拼接字符标志位
     *
     * Pair(8.8, B), Pair(8.8, K)
     */
    const val FLAG_NULL = 0

    /**
     * 最小单位为B
     */
    const val MIN_UNIT_BYTE = -1

    /**
     * 最小单位为KB
     */
    const val MIN_UNIT_KB = 0

    /**
     * 最小单位为MB
     */
    const val MIN_UNIT_MB = 1

    private const val CHAR_BYTE = 'B'
    private const val CHARS_INFIX_SECOND = "/s"

    private val UNIT_CHARS = charArrayOf('K', 'M', 'G', 'T', 'P', 'E', 'Z', 'Y', 'B', 'N', 'D')

    // android.text.format.Formatter.formatFileSize(android.content.Context, long)
    // 8.0以后使用的单位是1000，非1024
    private const val UNIT_SIZE = 1024

    private const val THRESHOLD = 900

    fun format(
        bytes: Long,
        @NetFlag flags: Int,
        @Accuracy accuracy: Int,
        @MinUnit minUnit: Int = MIN_UNIT_BYTE,
    ): Pair<String, String> {

        fun hasFlag(flag: Int): Boolean = (flags and flag) > 0

        val munit = min(max(MIN_UNIT_BYTE, minUnit), MIN_UNIT_MB)

        var speed = bytes.toDouble()
        var unit: Char = CHAR_BYTE
        for (i in UNIT_CHARS.indices) {
            if (munit >= i || speed > THRESHOLD) {
                speed /= UNIT_SIZE
                unit = UNIT_CHARS[i]
            } else {
                break
            }
        }

        val format = formatNumberInternal(speed, accuracy)// 速度

        val sb = StringBuilder()
            .append(unit)// 单位

        if (hasFlag(FLAG_BYTE) && unit != CHAR_BYTE) {
            sb.append(CHAR_BYTE)// 拼接B
        }
        if (hasFlag(FLAG_INFIX_SECOND)) {
            sb.append(CHARS_INFIX_SECOND)// 拼接/s
        }

        return format to sb.toString()
    }

    private fun formatNumberInternal(num: Double, @Accuracy accuracy: Int): String {
        val pattern = when (accuracy) {
            ACCURACY_EQUAL_WIDTH_EXACT -> when {
                num >= 100 -> "0" // 100.2 -> 100
                num >= 10 -> "0.#" // 10.22 -> 10.2
                else -> "0.##" // 0.223 -> 0.22
            }
            ACCURACY_EQUAL_WIDTH -> when {
                num >= 10 -> "0" // 10.2 -> 10
                else -> "0.#" // 1.22 -> 1.2
            }
            ACCURACY_EXACT -> "0.##" // 0.223 -> 0.22
            ACCURACY_SHORTER -> "0.#"
            else -> "0.##"
        }
        return DecimalFormat(pattern).format(num)
    }

    /**
     * 计算目标字节数最近的天花板整数字节
     */
    fun calculateCeilBytes(bytes: Long): Long {
        var speed = bytes.toDouble()
        var c = 0
        while (speed >= THRESHOLD) {
            speed /= UNIT_SIZE
            c++
        }
        if (c == 0) {
            return UNIT_SIZE.toLong()
        }

        return ceil(speed).toLong() * UNIT_SIZE.toDouble().pow(c.toDouble()).toLong()
    }

}
