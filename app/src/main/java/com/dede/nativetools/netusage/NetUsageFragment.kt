package com.dede.nativetools.netusage

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.dede.nativetools.R
import com.dede.nativetools.databinding.FragmentNetUsageBinding
import com.dede.nativetools.databinding.ItemNetUsageBinding
import com.dede.nativetools.util.ActivityResultLauncherCompat
import com.dede.nativetools.util.Logic
import com.dede.nativetools.util.UI

/**
 * 网络使用情况页面
 */
class NetUsageFragment : Fragment(R.layout.fragment_net_usage) {

    private val binding by viewBinding(FragmentNetUsageBinding::bind)
    private val viewModel by viewModels<NetUsageViewModel>()

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
        Logic.requestOpsPermission(requireContext(), activityResultLauncherCompat, granted = {
            viewModel.getNetUsage(requireContext())
                .observe(viewLifecycleOwner) { list: List<NetUsage> ->
                    binding.recyclerView.adapter = Adapter(list) {
                        binding.tvUsageDetail.text = it.formatString(requireContext())
                    }
                    binding.recyclerView.scrollToPosition(list.size - 1)
                }
            viewModel.getCoordinateMax().observe(viewLifecycleOwner) { max: Long ->
                val coordinateDrawable = NetUsageCoordinateDrawable(requireContext(), max)
                binding.recyclerView.background = coordinateDrawable
                binding.recyclerView.updatePadding(
                    left = coordinateDrawable.paddingLeft,
                    top = coordinateDrawable.paddingTop,
                    right = coordinateDrawable.paddingRight
                )
            }
        })
    }

    private class Adapter(
        val list: List<NetUsage>,
        val onItemSelectedListener: (netUsage: NetUsage) -> Unit,
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

}