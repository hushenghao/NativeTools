package com.dede.nativetools.netspeed.stats

import android.os.Build
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.URLSpan
import android.util.Log
import android.widget.Toast
import com.dede.nativetools.util.globalContext
import com.dede.nativetools.util.toast

/**
 * For Android T Previews
 *
 * TrafficStats getRxBytes and getTxBytes broken in T beta 3
 * https://issuetracker.google.com/issues/235454286
 * todo Check again.
 * @since 2022/7/12
 */
class AndroidTBP3NetStats : NetStats {

    private val regexBeta = "TPB\\d.\\d+.\\d+".toRegex()
    private val issuesUrl = "https://issuetracker.google.com/issues/235454286"

    override fun supported(): Boolean {
        if (Build.VERSION.SDK_INT == 33) {
            val version = Build.DISPLAY
            val androidVer = Build.VERSION.RELEASE
            Log.i("AndroidTBP3NetStats",
                "Android %s, Build Version: %s".format(androidVer, version))
            if (androidVer == "Tiramisu"// Developer Previews
                || version.matches(regexBeta)// Beta Releases
            ) {
                val span = SpannableStringBuilder()
                    .append("Android T Beta may not work!\n")
                    // Can't click
                    .append(issuesUrl, URLSpan(issuesUrl), Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
                globalContext.toast(span, Toast.LENGTH_LONG)
            }
        }
        return false
    }

    override fun getRxBytes(): Long {
        return NetStats.UNSUPPORTED
    }

    override fun getTxBytes(): Long {
        return NetStats.UNSUPPORTED
    }
}
