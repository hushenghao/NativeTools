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
 * For Android T Beta Releases
 *
 * TrafficStats getRxBytes and getTxBytes broken in T beta 3
 * https://issuetracker.google.com/issues/235454286
 * todo Check again.
 * @since 2022/7/12
 */
class AndroidTBP3NetStats : NetStats {

    private val previewVer = "Tiramisu"
    private val regexBeta = "TPB\\d.\\d+.\\d+".toRegex()
    private val issuesUrl = "https://issuetracker.google.com/issues/235454286"

    override fun supported(): Boolean {
        if (Build.VERSION.SDK_INT == 33/*Build.VERSION_CODES.T*/) {
            val version = Build.DISPLAY
            val androidVer = Build.VERSION.RELEASE
            Log.i("AndroidTBP3NetStats",
                "Android %s, Build Version: %s".format(androidVer, version))
            if (androidVer == previewVer// Developer Previews
                || version.matches(regexBeta)// Beta Releases
            ) {
                val span = SpannableStringBuilder("May not work on ")
                    .append("Android T Beta!\n", StyleSpan(Typeface.BOLD), SPAN_INCLUSIVE_EXCLUSIVE)
                    .append(issuesUrl, URLSpan(issuesUrl), SPAN_INCLUSIVE_EXCLUSIVE)// Can't click
                globalContext.toast(span, Toast.LENGTH_LONG)
            }
        }
        return false
    }

    override fun getRxBytes(): Long {
        throw UnsupportedOperationException("Unsupported NetStats!")
    }

    override fun getTxBytes(): Long {
        throw UnsupportedOperationException("Unsupported NetStats!")
    }
}
