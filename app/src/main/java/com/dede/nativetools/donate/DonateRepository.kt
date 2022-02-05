package com.dede.nativetools.donate

import androidx.annotation.DrawableRes
import com.dede.nativetools.R

class Payment(
    @DrawableRes
    val resId: Int,
    val title: String? = null,
)

class DonateRepository {

    fun getPaymentList(): List<Payment> {
        return arrayListOf(
            Payment(R.drawable.img_logo_eth, "ETH"),
            Payment(R.drawable.img_logo_wxpay),
            Payment(R.drawable.img_logo_alipay),
            Payment(R.drawable.img_logo_paypal)
        )
    }

}