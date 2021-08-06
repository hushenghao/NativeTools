package com.dede.nativetools.netspeed

import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import com.dede.nativetools.ui.MainActivity
import com.dede.nativetools.R
import com.dede.nativetools.netspeed.NetSpeedConfiguration.Companion.getInterval
import com.dede.nativetools.netspeed.utils.NetFormater
import com.dede.nativetools.util.Intent
import com.dede.nativetools.util.globalPreferences
import com.dede.nativetools.util.newTask
import com.dede.nativetools.util.splicing

@RequiresApi(Build.VERSION_CODES.N)
class NetTileService : TileService() {

    private val netSpeedHelper = NetSpeedHelper { rxSpeed, txSpeed ->
        update(rxSpeed, txSpeed)
    }

    override fun onStartListening() {
        val interval = globalPreferences.getInterval()
        netSpeedHelper.interval = interval
        netSpeedHelper.resume()
    }

    override fun onStopListening() {
        netSpeedHelper.pause()
    }

    private fun startMain() {
        val intent = Intent<MainActivity>(baseContext)
            .newTask()
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
            NetFormater.formatBytes(rxSpeed, NetFormater.FLAG_FULL, NetFormater.ACCURACY_EXACT).splicing()
        val uploadSpeedStr =
            NetFormater.formatBytes(txSpeed, NetFormater.FLAG_FULL, NetFormater.ACCURACY_EXACT).splicing()

        val tile = qsTile
        tile.state = Tile.STATE_ACTIVE
        tile.icon = Icon.createWithBitmap(
            NetTextIconFactory.createIconBitmap(
                rxSpeed,
                txSpeed,
                NetSpeedConfiguration.initialize()
            )
        )
        tile.label = getString(R.string.tile_net_speed_label, downloadSpeedStr, uploadSpeedStr)
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