package com.dede.nativetools.donate

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.dede.nativetools.R
import com.dede.nativetools.databinding.DialogFragmentDonateBinding
import com.dede.nativetools.databinding.ItemPaymentLayoutBinding
import com.dede.nativetools.main.WindowEdgeManager
import com.dede.nativetools.ui.SpaceItemDecoration
import com.dede.nativetools.util.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 捐赠页
 */
class DonateDialogFragment : BottomSheetDialogFragment() {

    private val binding by viewBinding(DialogFragmentDonateBinding::bind)

    private val viewModel by viewModels<DonateViewModel>()
    private val clickHandler = ClickHandler(this)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_fragment_donate, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.paymentList.observe(this) {
            binding.recyclerView.adapter = Adapter(it, clickHandler)
        }
        binding.recyclerView.addItemDecoration(SpaceItemDecoration(12.dp))
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            WindowEdgeManager(requireContext()).applyEdgeToEdge(this.window)
            val behavior = (this as BottomSheetDialog).behavior
            behavior.skipCollapsed = true
        }
    }

    private class Adapter(val data: List<Payment>, val clickHandler: ClickHandler) :
        RecyclerView.Adapter<Holder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
            val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_payment_layout, parent, false)
            return Holder(itemView, clickHandler)
        }

        override fun onBindViewHolder(holder: Holder, position: Int) {
            holder.bindViewData(data[position])
        }

        override fun getItemCount(): Int {
            return data.size
        }
    }

    private class Holder(view: View, val clickHandler: ClickHandler) :
        RecyclerView.ViewHolder(view) {
        private val binding = ItemPaymentLayoutBinding.bind(itemView)

        fun bindViewData(payment: Payment) {
            binding.root.setOnClickListener {
                clickHandler.handleClick(payment)
            }
            binding.root.setOnLongClickListener(clickHandler.createOnLongClickListener(payment))
            binding.ivLogo.setImageResource(payment.resId)
            if (isNightMode()) {
                binding.ivLogo.imageTintList = ColorStateList.valueOf(Color.WHITE)
            }
        }
    }

    private class ClickHandler(private val host: Fragment) {

        private val activityResultLauncherCompat =
            ActivityResultLauncherCompat(host, ActivityResultContracts.RequestMultiplePermissions())

        fun handleClick(payment: Payment) {
            val context = host.requireContext()
            when (payment.resId) {
                R.drawable.img_logo_alipay -> {
                    context.browse(R.string.url_alipay_payment_code)
                }
                R.drawable.img_logo_paypal -> {
                    context.browse(R.string.url_paypal_payment_code)
                }
                R.drawable.img_logo_wxpay -> {
                    context.toast(R.string.toast_wx_payment_tip)
                }
            }
        }

        fun createOnLongClickListener(payment: Payment): View.OnLongClickListener? {
            return when (payment.resId) {
                R.drawable.img_logo_wxpay ->
                    createOnLongClickSaveQrCodeListener(R.drawable.layer_wx_payment_code)
                else -> null
            }
        }

        private fun createOnLongClickSaveQrCodeListener(@DrawableRes resId: Int): View.OnLongClickListener {

            fun save() {
                host.lifecycleScope.launchWhenStarted {
                    val uri = saveToAlbum(host.requireContext(), resId)
                    if (uri != null) {
                        host.toast(R.string.toast_saved)
                    }
                }
            }

            return View.OnLongClickListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    save()
                    return@OnLongClickListener true
                }
                val permissions = arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                if (host.checkPermissions(*permissions)) {
                    save()
                    return@OnLongClickListener true
                }
                activityResultLauncherCompat.launch(permissions) {
                    if (it.values.find { r -> !r } != null) {
                        return@launch
                    }
                    save()
                }
                return@OnLongClickListener true
            }
        }

        private suspend fun saveToAlbum(context: Context, @DrawableRes resId: Int): Uri? =
            withContext(Dispatchers.IO) {
                context.requireDrawable<Drawable>(resId)
                    .toBitmap()
                    .saveToAlbum(context, "QrCode_${resId}.jpeg", "Net Monitor")
            }
    }
}