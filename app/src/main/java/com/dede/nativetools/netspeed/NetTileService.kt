package com.dede.nativetools.netspeed

import android.content.Intent
import android.graphics.Typeface
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import androidx.annotation.RequiresApi
import androidx.preference.PreferenceManager
import com.dede.nativetools.MainActivity
import com.dede.nativetools.R
import com.dede.nativetools.util.safeInt

@RequiresApi(Build.VERSION_CODES.N)
class NetTileService : TileService(), NetSpeedChanged {

    private var interval: Int = NetSpeedService.DEFAULT_INTERVAL
    private val sp by lazy { PreferenceManager.getDefaultSharedPreferences(baseContext) }
    private val speed = NetSpeed(this)

    override fun onStartListening() {
        interval = sp.getString(NetSpeedFragment.KEY_NET_SPEED_INTERVAL, null)
            .safeInt(NetSpeedService.DEFAULT_INTERVAL)
        speed.interval = interval
        speed.resume()
    }

    override fun onStopListening() {
        speed.pause()
    }

    private fun startMain() {
        val intent = Intent(baseContext, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivityAndCollapse(intent)
    }

    override fun onClick() {
        if (isLocked) {
            unlockAndRun { startMain() }
        } else {
            startMain()
        }
    }

    override fun invoke(rxSpeed: Long, txSpeed: Long) {
        val downloadSpeed = rxSpeed
        val uploadSpeed = txSpeed

        val downloadSpeedStr: String = NetUtil.formatNetSpeedStr(downloadSpeed)
        val uploadSpeedStr: String = NetUtil.formatNetSpeedStr(uploadSpeed)

        val tile = qsTile
        tile.state = Tile.STATE_ACTIVE
        val downSplit: Array<String> = NetUtil.formatNetSpeed(downloadSpeed)
        tile.icon = Icon.createWithBitmap(
            NetTextIconFactory.createSingleIcon(downSplit[0], downSplit[1])
        )
        val span = SpannableStringBuilder()
            .append("⇃", StyleSpan(Typeface.BOLD), Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
            .append(downloadSpeedStr)
            .append("\t")
            .append("↿", StyleSpan(Typeface.BOLD), Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
            .append(uploadSpeedStr)
        tile.label = span
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            tile.subtitle = getString(R.string.label_net_speed)
        }
        tile.updateTile()
    }

    override fun onDestroy() {
        speed.pause()
        super.onDestroy()
    }
}