package com.dede.nativetools.donate

import android.Manifest
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
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.dede.nativetools.R
import com.dede.nativetools.databinding.DialogFragmentDonateBinding
import com.dede.nativetools.databinding.ItemPaymentLayoutBinding
import com.dede.nativetools.ui.SpaceItemDecoration
import com.dede.nativetools.util.*
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 捐赠页
 */
class DonateDialogFragment : BottomSheetDialogFragment() {

    private val binding by viewBinding(DialogFragmentDonateBinding::bind)

    private val activityResultLauncherCompat =
        ActivityResultLauncherCompat(this, ActivityResultContracts.RequestMultiplePermissions())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_fragment_donate, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerView.adapter = Adapter(data)
        binding.recyclerView.addItemDecoration(SpaceItemDecoration(12.dp))
    }

    private class Payment(
        @DrawableRes
        val resId: Int,
        val click: View.OnClickListener,
        val longClick: View.OnLongClickListener? = null
    )

    private val data = arrayOf(
        Payment(
            R.drawable.img_logo_wxpay, {
                toast(R.string.toast_wx_payment_tip)
            }, createOnLongClickSaveQrCodeListener(R.drawable.img_wx_payment_code)
        ),
        Payment(
            R.drawable.img_logo_alipay, {
                it.context.browse(R.string.url_alipay_payment_code)
            }),
        Payment(
            R.drawable.img_logo_paypal, {
                it.context.browse(R.string.url_paypal_payment_code)
            })
    )

    private class Adapter(val data: Array<Payment>) : RecyclerView.Adapter<Holder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
            val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_payment_layout, parent, false)
            return Holder(itemView)
        }

        override fun onBindViewHolder(holder: Holder, position: Int) {
            holder.bindViewData(data[position])
        }

        override fun getItemCount(): Int {
            return data.size
        }
    }

    private class Holder(view: View) : RecyclerView.ViewHolder(view) {
        private val binding = ItemPaymentLayoutBinding.bind(itemView)

        fun bindViewData(payment: Payment) {
            binding.root.setOnClickListener(payment.click)
            binding.root.setOnLongClickListener(payment.longClick)
            binding.ivLogo.setImageResource(payment.resId)
            if (isNightMode()) {
                binding.ivLogo.imageTintList = ColorStateList.valueOf(Color.WHITE)
            }
        }
    }

    private fun createOnLongClickSaveQrCodeListener(@DrawableRes resId: Int): View.OnLongClickListener {

        val func = Runnable {
            lifecycleScope.launchWhenStarted {
                val uri = saveToAlbum(requireContext(), resId)
                if (uri != null) {
                    toast(R.string.toast_saved)
                }
            }
        }

        return View.OnLongClickListener {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                val permissions = arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                if (!checkPermissions(*permissions)) {
                    activityResultLauncherCompat.launch(permissions) {
                        if (it.values.find { r -> !r } != null) {
                            return@launch
                        }
                        func.run()
                    }
                    return@OnLongClickListener true
                }
            }
            func.run()
            return@OnLongClickListener true
        }
    }

    private suspend fun saveToAlbum(context: Context, @DrawableRes resId: Int): Uri? =
        withContext(Dispatchers.IO) {
            context.requireDrawable<Drawable>(resId)
                .toBitmap()
                .saveToAlbum(requireContext(), "QrCode_${resId}.jpeg", "Net Monitor")
        }

}