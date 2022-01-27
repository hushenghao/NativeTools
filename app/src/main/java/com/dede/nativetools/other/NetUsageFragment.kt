package com.dede.nativetools.other

import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.dede.nativetools.R
import com.dede.nativetools.databinding.FragmentNetUsageBinding
import com.dede.nativetools.netspeed.utils.NetFormatter
import com.dede.nativetools.netspeed.utils.NetworkUsageUtil.queryNetworkUsageBucket
import com.dede.nativetools.util.requireSystemService
import com.dede.nativetools.util.splicing
import com.dede.nativetools.util.toZeroH
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class NetUsageFragment : Fragment(R.layout.fragment_net_usage) {

    private val binding by viewBinding(FragmentNetUsageBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launchWhenStarted {
            val manager = requireContext().requireSystemService<NetworkStatsManager>()
            val start = Calendar.getInstance().toZeroH()
            binding.tvTodayUsage.text = loadNetUsage(manager, start.timeInMillis)
            start.set(Calendar.DAY_OF_MONTH, 1)
            binding.tvMonthUsage.text = loadNetUsage(manager, start.timeInMillis)
            binding.tvAllUsage.text = loadNetUsage(manager, 0)
        }
    }

    private suspend fun loadNetUsage(
        manager: NetworkStatsManager,
        start: Long,
    ): String {
        @Suppress("DEPRECATION")
        return withContext(Dispatchers.IO) {
            val sb = StringBuilder()
            val end = System.currentTimeMillis()
            var bucket =
                manager.queryNetworkUsageBucket(ConnectivityManager.TYPE_MOBILE, start, end)
            sb.appendBucket("Mobile", bucket)

            sb.appendLine()
            bucket = manager.queryNetworkUsageBucket(ConnectivityManager.TYPE_WIFI, start, end)
            sb.appendBucket("Wi-Fi", bucket)

            return@withContext sb.toString()
        }
    }

    private fun StringBuilder.appendBucket(name: String, bucket: NetworkStats.Bucket?) {
        this.append(name)
            .append(": ↑")
            .append(formatUsage(bucket?.txBytes))
            .append(", ↓️")
            .append(formatUsage(bucket?.rxBytes))
    }

    private fun formatUsage(bytes: Long?): String {
        if (bytes == null) return "--"
        return NetFormatter.format(bytes, NetFormatter.FLAG_BYTE, NetFormatter.ACCURACY_EXACT)
            .splicing()
    }


    override fun onDestroyView() {
        super.onDestroyView()
    }
}