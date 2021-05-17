package com.dede.nativetools.ui.netspeed

import android.content.Intent
import android.graphics.Typeface
import android.graphics.drawable.Icon
import android.net.TrafficStats
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Message
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
class NetTileService : TileService(), Handler.Callback, Runnable {

    private val handler = Handler(Looper.getMainLooper(), this)
    private var rxBytes: Long = 0L
    private var txBytes: Long = 0L

    private var interval: Int = NetSpeedService.DEFAULT_INTERVAL
    private val sp by lazy { PreferenceManager.getDefaultSharedPreferences(baseContext) }

    override fun onStartListening() {
        interval = sp.getString(NetSpeedFragment.KEY_NET_SPEED_INTERVAL, null)
            .safeInt(NetSpeedService.DEFAULT_INTERVAL)
        rxBytes = TrafficStats.getTotalRxBytes()
        txBytes = TrafficStats.getTotalTxBytes()
        handler.post(this)
    }

    override fun onStopListening() {
        handler.removeCallbacks(this)
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

    override fun handleMessage(msg: Message): Boolean {
        return true
    }

    override fun run() {
        val rxBytes = TrafficStats.getTotalRxBytes()
        val txBytes = TrafficStats.getTotalTxBytes()
        val downloadSpeed = ((rxBytes - this.rxBytes) * 1f / interval * 1000 + .5).toLong()
        val uploadSpeed = ((txBytes - this.txBytes) * 1f / interval * 1000 + .5).toLong()

        this.txBytes = txBytes
        this.rxBytes = rxBytes

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

        handler.postDelayed(this, interval.toLong())
    }

    override fun onDestroy() {
        handler.removeCallbacks(this)
        super.onDestroy()
    }
}