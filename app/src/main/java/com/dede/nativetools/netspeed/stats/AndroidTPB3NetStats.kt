package com.dede.nativetools.netspeed.stats

import android.graphics.Typeface
import android.os.Build
import android.text.Spannable.SPAN_INCLUSIVE_EXCLUSIVE
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.text.style.URLSpan
import android.util.Log
import android.widget.Toast
import com.dede.nativetools.util.globalContext
import com.dede.nativetools.util.toast

/**
 * For Android T Beta 3 Releases
 *
 * TrafficStats getRxBytes and getTxBytes broken in T beta 3
 * https://issuetracker.google.com/issues/235454286 Fixed in T beta 4, TPB4.220624.004
 *
 * @since 2022/7/12
 */
class AndroidTPB3NetStats : NetStats {

    private val regexBeta = "TPB(\\d).\\d+.\\d+".toRegex()
    private val issuesUrl = "https://issuetracker.google.com/issues/235454286"
    private val fixedVer = 4

    override fun supported(): Boolean {
        if (Build.VERSION.SDK_INT == 33 /*Build.VERSION_CODES.T*/) {
            val version = Build.DISPLAY
            val androidVer = Build.VERSION.RELEASE
            Log.i(
                "AndroidTBP3NetStats", "Android %s, Build Version: %s".format(androidVer, version))
            val ver = getTPBVersion()
            if (ver < fixedVer) {
                val span =
                    SpannableStringBuilder("May not work on ")
                        .append(
                            "Android T Beta $ver !\n",
                            StyleSpan(Typeface.BOLD),
                            SPAN_INCLUSIVE_EXCLUSIVE)
                        .append(
                            issuesUrl, URLSpan(issuesUrl), SPAN_INCLUSIVE_EXCLUSIVE) // Can't click
                globalContext.toast(span, Toast.LENGTH_LONG)
            }
        }
        return false
    }

    private fun getTPBVersion(): Int {
        // Beta Releases, like this 'TPB3.220513.017'
        // TrafficStats getRxBytes and getTxBytes broken in T beta 3.
        // Fixed in T beta 4, TPB4.220624.004
        return regexBeta.matchEntire(Build.DISPLAY)?.groupValues?.getOrNull(1)?.toIntOrNull()
            ?: fixedVer
    }

    override fun getRxBytes(): Long {
        throw UnsupportedOperationException("Unsupported NetStats!")
    }

    override fun getTxBytes(): Long {
        throw UnsupportedOperationException("Unsupported NetStats!")
    }
}
