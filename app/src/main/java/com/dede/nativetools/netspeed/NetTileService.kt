package com.dede.nativetools.netspeed

import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import androidx.preference.PreferenceManager
import com.dede.nativetools.MainActivity
import com.dede.nativetools.R
import com.dede.nativetools.util.safeInt

@RequiresApi(Build.VERSION_CODES.N)
class NetTileService : TileService() {

    private var interval: Int = NetSpeedService.DEFAULT_INTERVAL
    private val sp by lazy { PreferenceManager.getDefaultSharedPreferences(baseContext) }
    private val speed = NetSpeed { rxSpeed, txSpeed ->
        update(rxSpeed, txSpeed)
    }

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

    private fun update(rxSpeed: Long, txSpeed: Long) {
        val downloadSpeedStr: String = NetUtil.formatNetSpeedStr(rxSpeed)
        val uploadSpeedStr: String = NetUtil.formatNetSpeedStr(txSpeed)

        val tile = qsTile
        tile.state = Tile.STATE_ACTIVE
        val downSplit: Array<String> = NetUtil.formatNetSpeed(rxSpeed)
        tile.icon = Icon.createWithBitmap(
            NetTextIconFactory.createSingleIcon(downSplit[0], downSplit[1])
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
        speed.pause()
        super.onDestroy()
    }
}