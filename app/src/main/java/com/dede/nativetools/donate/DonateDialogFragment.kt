package com.dede.nativetools.donate

import android.app.Dialog
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.core.view.ViewCompat
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.dede.nativetools.R
import com.dede.nativetools.databinding.DialogFragmentDonateBinding
import com.dede.nativetools.util.browse
import com.dede.nativetools.util.saveToAlbum
import com.dede.nativetools.util.toast
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

/**
 * 捐赠页
 */
class DonateDialogFragment : BottomSheetDialogFragment() {

    private val binding by viewBinding(DialogFragmentDonateBinding::bind)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_fragment_donate, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.ivAlipay.setOnClickListener {
            requireContext().browse(R.string.url_alipay_payment_code)
        }
        binding.ivWxpay.setOnClickListener {
            toast(R.string.toast_wx_payment_tip)
        }
        binding.ivWxpay.setOnLongClickListener(createOnQrCodeLongClickListener(R.drawable.wx_payment_code))
        binding.ivAlipay.setOnLongClickListener(createOnQrCodeLongClickListener(R.drawable.alipay_payment_code))

        binding.ivWxpay.run {
            post {
                val padding = (1.5f / 51.5f * width).roundToInt()
                ViewCompat.setPaddingRelative(this, padding, padding, padding, padding)
                background = GradientDrawable().apply {
                    setStroke(padding, Color.WHITE)
                }
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.behavior.apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            skipCollapsed = true
        }
        return dialog
    }

    private fun createOnQrCodeLongClickListener(@DrawableRes resId: Int): View.OnLongClickListener {
        return View.OnLongClickListener {
            lifecycleScope.launchWhenStarted {
                val uri = saveToAlbum(requireContext(), resId)
                if (uri != null) {
                    toast(R.string.toast_saved)
                }
            }
            return@OnLongClickListener true
        }
    }

    private suspend fun saveToAlbum(context: Context, @DrawableRes resId: Int): Uri? =
        withContext(Dispatchers.IO) {
            val bitmap =
                BitmapFactory.decodeResource(context.resources, resId) ?: return@withContext null
            bitmap.saveToAlbum(requireContext(), "QrCode_${resId}.jpeg", "Net Monitor")
        }

}