package com.dede.nativetools.netspeed

import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import com.dede.nativetools.MainActivity
import com.dede.nativetools.R
import com.dede.nativetools.netspeed.NetSpeedConfiguration.Companion.defaultSharedPreferences
import com.dede.nativetools.util.safeInt
import com.dede.nativetools.util.splicing

@RequiresApi(Build.VERSION_CODES.N)
class NetTileService : TileService() {

    private var interval: Int = NetSpeedConfiguration.DEFAULT_INTERVAL

    private val netSpeedHelper = NetSpeedHelper { rxSpeed, txSpeed ->
        update(rxSpeed, txSpeed)
    }

    override fun onStartListening() {
        interval = defaultSharedPreferences.getString(NetSpeedConfiguration.KEY_NET_SPEED_INTERVAL, null)
            .safeInt(NetSpeedConfiguration.DEFAULT_INTERVAL)
        netSpeedHelper.interval = interval
        netSpeedHelper.resume()
    }

    override fun onStopListening() {
        netSpeedHelper.pause()
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

    private fun update(rxSpeed: Long, txSpeed: Long) {
        val downloadSpeedStr =
            NetUtil.formatBytes(rxSpeed, NetUtil.FLAG_FULL, NetUtil.ACCURACY_EXACT).splicing()
        val uploadSpeedStr =
            NetUtil.formatBytes(txSpeed, NetUtil.FLAG_FULL, NetUtil.ACCURACY_EXACT).splicing()

        val tile = qsTile
        tile.state = Tile.STATE_ACTIVE
        val downSplit =
            NetUtil.formatBytes(rxSpeed, NetUtil.FLAG_FULL, NetUtil.ACCURACY_EQUAL_WIDTH_EXACT)
        tile.icon = Icon.createWithBitmap(
            NetTextIconFactory.createSingleIcon(downSplit.first, downSplit.second)
        )
        val builder = StringBuilder()
            .append("⇃")
            .append(downloadSpeedStr)
            .append("\t↿")
            .append(uploadSpeedStr)
        tile.label = builder.toString()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            tile.subtitle = getString(R.string.label_net_speed)
        }
        tile.updateTile()
    }

    override fun onDestroy() {
        netSpeedHelper.pause()
        super.onDestroy()
    }
}