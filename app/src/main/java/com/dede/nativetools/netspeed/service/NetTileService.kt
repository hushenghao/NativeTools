package com.dede.nativetools.netspeed.service

import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import com.dede.nativetools.R
import com.dede.nativetools.main.MainActivity
import com.dede.nativetools.netspeed.NetSpeedConfiguration
import com.dede.nativetools.netspeed.utils.NetFormatter
import com.dede.nativetools.netspeed.utils.NetSpeedCompute
import com.dede.nativetools.netspeed.utils.NetTextIconFactory
import com.dede.nativetools.util.Intent
import com.dede.nativetools.util.globalDataStore
import com.dede.nativetools.util.newTask
import com.dede.nativetools.util.splicing
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.N)
class NetTileService : TileService() {

    private val configuration = NetSpeedConfiguration()

    private val netSpeedCompute = NetSpeedCompute { rxSpeed, txSpeed -> update(rxSpeed, txSpeed) }
    private val lifecycleJob = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.IO + lifecycleJob)

    override fun onStartListening() {
        coroutineScope.launch {
            val preferences = globalDataStore.data.firstOrNull() ?: return@launch
            configuration.updateFrom(preferences)
        }
        netSpeedCompute.start()
    }

    override fun onStopListening() {
        netSpeedCompute.stop()
    }

    private fun startMain() {
        val intent = Intent<MainActivity>(baseContext).newTask()
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
        val qsTile = qsTile ?: return
        val downloadSpeedStr =
            NetFormatter.format(rxSpeed, NetFormatter.FLAG_FULL, NetFormatter.ACCURACY_EXACT)
                .splicing()
        val uploadSpeedStr =
            NetFormatter.format(txSpeed, NetFormatter.FLAG_FULL, NetFormatter.ACCURACY_EXACT)
                .splicing()

        qsTile
            .apply {
                state = Tile.STATE_ACTIVE
                val bitmap = NetTextIconFactory.create(rxSpeed, txSpeed, configuration)
                icon = Icon.createWithBitmap(bitmap)
                label = getString(R.string.tile_net_speed_label, uploadSpeedStr, downloadSpeedStr)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    subtitle = getString(R.string.label_net_speed)
                }
            }
            .updateTile()
    }

    override fun onDestroy() {
        lifecycleJob.cancel()
        netSpeedCompute.destroy()
        super.onDestroy()
    }
}
