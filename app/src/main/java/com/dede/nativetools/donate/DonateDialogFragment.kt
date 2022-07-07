package com.dede.nativetools.donate

import android.Manifest
import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.dede.nativetools.R
import com.dede.nativetools.databinding.ItemBottomSheetListBinding
import com.dede.nativetools.ui.BottomSheetListFragment
import com.dede.nativetools.util.*
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 捐赠页
 */
class DonateDialogFragment : BottomSheetListFragment<Payment>() {

    private val viewModel by viewModels<DonateViewModel>()
    private val clickHandler = ClickHandler(this)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.paymentList.observe(this) {
            setData(it)
        }
        binding.tvTitle.isGone = true
        binding.tvMessage.isGone = true
    }

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override fun onBindViewHolder(
        binding: ItemBottomSheetListBinding,
        position: Int,
        payment: Payment,
    ) {
        binding.root.setOnClickListener {
            clickHandler.handleClick(payment)
        }
        binding.root.setOnLongClickListener(clickHandler.createOnLongClickListener(payment))
        binding.ivLogo.setImageResource(payment.resId)
        binding.tvTitle.setText(payment.textId)
    }

    private class ClickHandler(private val host: Fragment) {

        private val activityResultLauncherCompat =
            ActivityResultLauncherCompat(host, ActivityResultContracts.RequestMultiplePermissions())

        fun handleClick(payment: Payment) {
            val context = host.requireContext()
            var name = ""
            when (payment.resId) {
                R.drawable.ic_logo_eth -> {
                    context.copy(R.string.payment_eth_address)
                    name = "ETH"
                }
                R.drawable.ic_logo_alipay -> {
                    context.browse(R.string.url_alipay_payment_code)
                    context.toast(R.string.toast_payment_tip)
                    name = "支付宝"
                }
                R.drawable.ic_logo_paypal -> {
                    context.browse(R.string.url_paypal_payment_code)
                    name = "PayPal"
                }
                R.drawable.ic_logo_wxpay -> {
                    context.toast(R.string.toast_payment_tip)
                    name = "微信"
                }
                R.drawable.ic_more_vert -> {
                    host.findNavController().navigate(R.id.action_dialogDonate_to_dialogDonateList)
                    return
                }
            }
            event(FirebaseAnalytics.Event.SELECT_ITEM) {
                param(FirebaseAnalytics.Param.ITEM_NAME, name)
                param(FirebaseAnalytics.Param.CONTENT_TYPE, "捐赠")
            }
        }

        fun createOnLongClickListener(payment: Payment): View.OnLongClickListener? {
            return when (payment.resId) {
                R.drawable.ic_logo_wxpay ->
                    createOnLongClickSaveQrCodeListener(R.drawable.layer_wx_payment_code)
                R.drawable.ic_logo_alipay ->
                    createOnLongClickSaveQrCodeListener(R.drawable.layer_alipay_code)
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