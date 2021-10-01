package com.dede.nativetools.donate

import android.app.Dialog
import android.content.res.TypedArray
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.TypedArrayUtils
import androidx.fragment.app.FragmentManager
import by.kirich1409.viewbindingdelegate.viewBinding
import com.dede.nativetools.R
import com.dede.nativetools.databinding.DialogFragmentDonateBinding
import com.dede.nativetools.util.browse
import com.dede.nativetools.util.toast
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * 捐赠页
 */
class DonateDialogFragment : BottomSheetDialogFragment() {

    companion object {
        fun show(manager: FragmentManager) {
            val donateDialogFragment = DonateDialogFragment()
            donateDialogFragment.show(manager, "Donate")
        }
    }

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
            toast("微信付款码请截图后扫描")
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

}