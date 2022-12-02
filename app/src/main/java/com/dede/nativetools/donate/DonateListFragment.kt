package com.dede.nativetools.donate

import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dede.nativetools.R
import com.dede.nativetools.databinding.ItemDonateListInfoBinding
import com.dede.nativetools.ui.AbsBottomSheetListFragment
import com.dede.nativetools.ui.GridItemDecoration
import com.dede.nativetools.util.dp
import com.dede.nativetools.util.emailTo
import com.dede.nativetools.util.isEmpty
import java.text.DateFormat
import java.util.*

/**
 * 捐助列表
 *
 * @since 2022/7/1
 */
class DonateListFragment : AbsBottomSheetListFragment<DonateInfo, DonateListFragment.ViewHolder>() {

    private val viewModel by viewModels<DonateListViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val span = SpannableStringBuilder()
            .append(requireContext().getText(R.string.message_donate_not_fount),
                object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        requireContext().emailTo(R.string.email)
                    }
                },
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.tvMessage.text = span
        binding.tvMessage.movementMethod = LinkMovementMethod.getInstance()

        binding.tvTitle.setText(R.string.label_donate_list)
        binding.recyclerView.addItemDecoration(GridItemDecoration(12.dp))
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        viewModel.donateList.observe(this) { setData(it) }
    }

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val itemView =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_donate_list_info, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, t: DonateInfo) {
        holder.bindViewData(t)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val bind = ItemDonateListInfoBinding.bind(view)

        fun bindViewData(info: DonateInfo) {
            bind.tvDonatePrice.text = info.donatePrice
            bind.tvDonateName.text = info.donateUser
            bind.tvDonateChannel.text = info.donateChannel
            bind.tvDonateOrder.text = info.orderId
            val dateFormat = DateFormat.getDateTimeInstance()
            bind.tvDonateDate.text = dateFormat.format(Date(info.donateTimestamp * 1000))
            if (info.donateRemake.isEmpty()) {
                bind.tvDonateRemake.isVisible = false
            } else {
                bind.tvDonateRemake.text = info.donateRemake
                bind.tvDonateRemake.isVisible = true
            }
        }
    }
}
