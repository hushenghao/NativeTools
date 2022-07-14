package com.dede.nativetools.donate

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.dede.nativetools.R

class Payment(
    @DrawableRes
    val resId: Int,
    @StringRes
    val textId: Int,
)

class DonateRepository {

    fun getPaymentList(): List<Payment> {
        return arrayListOf(
            Payment(R.drawable.ic_logo_wxpay, R.string.label_payment_wxpay),
            Payment(R.drawable.ic_logo_alipay, R.string.label_payment_alipay),
            Payment(R.drawable.ic_logo_paypal, R.string.label_payment_paypal),
            Payment(R.drawable.ic_logo_eth, R.string.label_payment_eth),
            Payment(R.drawable.ic_donate_history, R.string.label_donate_list)
        )
    }

}