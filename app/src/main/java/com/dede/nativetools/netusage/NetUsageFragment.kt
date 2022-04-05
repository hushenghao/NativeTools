package com.dede.nativetools.netusage

import android.app.usage.NetworkStatsManager
import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.dede.nativetools.R
import com.dede.nativetools.databinding.FragmentNetUsageBinding
import com.dede.nativetools.databinding.ItemNetUsageBinding
import com.dede.nativetools.netspeed.utils.NetFormatter
import com.dede.nativetools.netspeed.utils.NetworkUsageUtil.queryNetworkUsageBucket
import com.dede.nativetools.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.math.max
import kotlin.math.roundToInt

class NetUsageFragment : Fragment(R.layout.fragment_net_usage) {

    private val binding by viewBinding(FragmentNetUsageBinding::bind)

    private val activityResultLauncherCompat =
        ActivityResultLauncherCompat(this, ActivityResultContracts.StartActivityForResult())

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (UI.isWideSize()) {
            binding.llRoot.orientation = LinearLayout.HORIZONTAL
            val block: LinearLayout.LayoutParams.() -> Unit = {
                gravity = Gravity.CENTER_VERTICAL
                weight = 1f
                width = 0
            }
            binding.clContent.updateLayoutParams(block)
            binding.tvUsageDetail.updateLayoutParams(block)
        }

        binding.recyclerView.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        binding.recyclerView.itemAnimator = null
        binding.tvStart.text =
            NetFormatter.format(0L, NetFormatter.FLAG_NULL, NetFormatter.ACCURACY_SHORTER)
                .splicing()
        Logic.requestOpsPermission(requireContext(), activityResultLauncherCompat, granted = {
            lifecycleScope.launchWhenStarted {
                val manager = requireContext().requireSystemService<NetworkStatsManager>()
                val end = Calendar.getInstance()
                val start = Calendar.getInstance().toZeroH()
                start.set(Calendar.DAY_OF_MONTH, 1)
                start.add(Calendar.MONTH, -5)// 5个月前
                val list = getMonthDateRanges(start, end).map {
                    loadNetUsage(manager, it.first.timeInMillis, it.second.timeInMillis)
                }.toMutableList()
                // 添加今日流量
                val todayDateRange = getTodayDateRange()
                list.add(
                    loadNetUsage(
                        manager,
                        todayDateRange.first.timeInMillis,
                        todayDateRange.second.timeInMillis,
                        getString(R.string.label_today)
                    )
                )
                val max = calculateMax(list)
                binding.tvLabelYMax.text =
                    NetFormatter.format(max, NetFormatter.FLAG_NULL, NetFormatter.ACCURACY_SHORTER)
                        .splicing()
                binding.tvLabelYCenter.text =
                    NetFormatter.format(
                        max / 2,
                        NetFormatter.FLAG_NULL,
                        NetFormatter.ACCURACY_SHORTER
                    ).splicing()
                for (netUsage in list) {
                    netUsage.calculateProgress(max)
                }
                binding.recyclerView.adapter = Adapter(list) {
                    binding.tvUsageDetail.text = it.formatString(requireContext())
                }
                binding.recyclerView.scrollToPosition(list.size - 1)
            }
        })
    }

    /**
     * 计算坐标系最大范围
     */
    private fun calculateMax(list: List<NetUsage>): Long {
        var max: Long = 0
        for (netUsage in list) {
            max = max(netUsage.currentMax, max)
        }
        return NetFormatter.calculateCeilBytes(max)// 取天花板数
    }

    private data class NetUsage(
        val start: Long,
        val end: Long,

        val wlanUpload: Long,
        val wlanDownload: Long,

        val mobileUpload: Long,
        val mobileDownload: Long,

        var label: String
    ) {
        val currentMax: Long
            get() = max(max(wlanUpload, wlanDownload), max(mobileUpload, mobileDownload))

        var wlanDownloadProgress: Int = 0
            private set
        var wlanUploadProgress: Int = 0
            private set

        var mobileDownloadProgress: Int = 0
            private set
        var mobileUploadProgress: Int = 0
            private set

        /**
         * 计算相对max的百分比，max可能为0
         */
        fun calculateProgress(max: Long) {
            if (max <= 0) return
            wlanDownloadProgress = (wlanDownload * 100f / max).roundToInt()
            wlanUploadProgress = (wlanUpload * 100f / max).roundToInt()
            mobileDownloadProgress = (mobileDownload * 100f / max).roundToInt()
            mobileUploadProgress = (mobileUpload * 100f / max).roundToInt()
        }

        private fun formatUsage(bytes: Long?): String {
            if (bytes == null) return "--"
            return NetFormatter.format(bytes, NetFormatter.FLAG_BYTE, NetFormatter.ACCURACY_EXACT)
                .splicing()
        }

        fun formatString(context: Context): String {
            val sb = StringBuilder()
                .append(label)
                .append(": ")
                .appendLine()
                .append("WLAN: \t")
                .append(
                    context.getString(
                        R.string.notify_net_speed_msg,
                        formatUsage(wlanUpload),
                        formatUsage(wlanDownload)
                    )
                )
                .appendLine()
                .append("Mobile: \t")
                .append(
                    context.getString(
                        R.string.notify_net_speed_msg,
                        formatUsage(mobileUpload),
                        formatUsage(mobileDownload)
                    )
                )
                .appendLine()
                .append("Total: \t")
                .append(
                    context.getString(
                        R.string.notify_net_speed_msg,
                        formatUsage(wlanUpload + mobileUpload),
                        formatUsage(wlanDownload + mobileDownload)
                    )
                )
            return sb.toString()
        }

    }

    private class Adapter(
        val list: List<NetUsage>,
        val onItemSelectedListener: (netUsage: NetUsage) -> Unit
    ) : RecyclerView.Adapter<Holder>() {

        private var selectedPosition = list.size - 2

        init {
            if (selectedPosition >= 0) {
                onItemSelectedListener.invoke(list[selectedPosition])
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
            return Holder(
                ItemNetUsageBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }

        override fun onBindViewHolder(holder: Holder, position: Int) {
            holder.bind(list[position], selectedPosition == position)
            holder.itemView.setOnClickListener {
                val p = holder.layoutPosition
                if (selectedPosition == p) {
                    return@setOnClickListener
                }
                val old = selectedPosition
                selectedPosition = p
                notifyItemChanged(old)
                notifyItemChanged(p)
                onItemSelectedListener.invoke(list[p])
            }
        }

        override fun getItemCount(): Int {
            return list.size
        }
    }

    private class Holder(private val binding: ItemNetUsageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(netUsage: NetUsage, isSelected: Boolean) {
            // 通常情况下
            // 下载的数据比上传的多，背景中progress显示在secondaryProgress下面
            // 所以progress显示下载，secondaryProgress显示上传
            binding.pbWlanUsage.progress = netUsage.wlanDownloadProgress
            binding.pbWlanUsage.secondaryProgress = netUsage.wlanUploadProgress

            binding.pbMobileUsage.progress = netUsage.mobileDownloadProgress
            binding.pbMobileUsage.secondaryProgress = netUsage.mobileUploadProgress

            binding.tvLabel.text = netUsage.label
            binding.vSelectedIndicator.isVisible = isSelected
        }
    }

    // 获取最近6个月中每个月时间范围
    private fun getMonthDateRanges(start: Calendar, end: Calendar): List<Pair<Calendar, Calendar>> {
        val result = mutableListOf<Pair<Calendar, Calendar>>()
        var current = start
        while (current.before(end)) {
            val next = Calendar.getInstance()
            next.timeInMillis = current.timeInMillis
            next.add(Calendar.MONTH, 1)
            result.add(Pair(current, next))
            current = next
        }
        return result
    }

    /**
     * 获取今天的时间范围
     */
    private fun getTodayDateRange(): Pair<Calendar, Calendar> {
        val start = Calendar.getInstance().toZeroH()
        val end = Calendar.getInstance()
        return Pair(start, end)
    }

    private suspend fun loadNetUsage(
        manager: NetworkStatsManager,
        start: Long,
        end: Long,
        label: String = "%tb".format(Date(start))
    ): NetUsage {
        @Suppress("DEPRECATION")
        return withContext(Dispatchers.IO) {
            val bucketMobile =
                manager.queryNetworkUsageBucket(ConnectivityManager.TYPE_MOBILE, start, end)
            val bucketWlan =
                manager.queryNetworkUsageBucket(ConnectivityManager.TYPE_WIFI, start, end)
            return@withContext NetUsage(
                start = start,
                end = end,
                wlanUpload = bucketWlan?.rxBytes ?: 0L,
                wlanDownload = bucketWlan?.txBytes ?: 0L,
                mobileUpload = bucketMobile?.rxBytes ?: 0L,
                mobileDownload = bucketMobile?.txBytes ?: 0L,
                label = label
            )
        }
    }

}