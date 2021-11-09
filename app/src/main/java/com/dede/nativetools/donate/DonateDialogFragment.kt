package com.dede.nativetools.donate

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.core.view.ViewCompat
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.dede.nativetools.R
import com.dede.nativetools.databinding.DialogFragmentDonateBinding
import com.dede.nativetools.util.*
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
        binding.ivAlipay.setOnClickListener {
            requireContext().browse(R.string.url_alipay_payment_code)
        }
        binding.ivWxpay.setOnClickListener {
            toast(R.string.toast_wx_payment_tip)
        }
        binding.ivWxpay.setOnLongClickListener(createOnLongClickSaveQrCodeListener(R.drawable.wx_payment_code))
        binding.ivAlipay.setOnLongClickListener(createOnLongClickSaveQrCodeListener(R.drawable.alipay_payment_code))

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
            val bitmap =
                BitmapFactory.decodeResource(context.resources, resId) ?: return@withContext null
            bitmap.saveToAlbum(requireContext(), "QrCode_${resId}.jpeg", "Net Monitor")
        }

}