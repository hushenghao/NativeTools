package com.dede.nativetools.netspeed.utils


import com.dede.nativetools.util.trimZeroAndDot


/**
 * 字节数格式化工具
 *
 * Created by hsh on 2017/5/15 015 下午 05:14.
 */
object NetFormatter {

    /**
     * 精确等宽格式
     */
    const val ACCURACY_EQUAL_WIDTH_EXACT = 1

    /**
     * 精确格式
     */
    const val ACCURACY_EXACT = 2

    /**
     * 低精度格式
     */
    const val ACCURACY_SHORTER = 4

    /**
     * 等宽格式
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

    private const val CHAR_BYTE = 'B'
    private const val CHARS_INFIX_SECOND = "/s"

    private val UNIT_CHARS = charArrayOf('K', 'M', 'G', 'T', 'P', 'E', 'Z', 'Y', 'B', 'N', 'D')

    // android.text.format.Formatter.formatFileSize(android.content.Context, long)
    // 8.0以后使用的单位是1000，非1024
    private const val UNIT_SIZE = 1024
    private const val THRESHOLD = 900

    fun format(bytes: Long, flags: Int, accuracy: Int): Pair<String, String> {

        fun hasFlag(flag: Int): Boolean = (flags and flag) > 0

        var speed = bytes.toDouble()
        var unit: Char = CHAR_BYTE
        for (char in UNIT_CHARS) {
            if (speed > THRESHOLD) {
                speed /= UNIT_SIZE
                unit = char
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


    private fun formatNumberInternal(num: Double, accuracy: Int): String {
        val format = when (accuracy) {
            ACCURACY_EQUAL_WIDTH_EXACT -> when {
                num >= 100 -> "%.0f" // 100.2 -> 100
                num >= 10 -> "%.1f" // 10.22 -> 10.2
                else -> "%.2f" // 0.223 -> 0.22
            }
            ACCURACY_EQUAL_WIDTH -> when {
                num >= 10 -> "%.0f" // 10.2 -> 10
                else -> "%.1f" // 1.22 -> 1.2
            }
            ACCURACY_EXACT -> "%.2f" // 0.223 -> 0.22
            ACCURACY_SHORTER -> "%.1f"
            else -> "%.2f"
        }
        return format.format(num).trimZeroAndDot()
    }

}
