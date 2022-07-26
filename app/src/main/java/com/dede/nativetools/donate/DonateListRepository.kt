package com.dede.nativetools.donate

import androidx.annotation.Keep
import com.dede.nativetools.network.Api
import com.squareup.moshi.Json

/**
 * Created by shhu on 2022/7/1 13:43.
 *
 * @since 2022/7/1
 */
@Keep
data class DonateInfo(
    @Json(name = "order_id") val orderId: String? = null,
    @Json(name = "donate_price") val donatePrice: String? = null,
    @Json(name = "donate_channel") val donateChannel: String? = null,
    @Json(name = "donate_user") val donateUser: String? = null,
    @Json(name = "donate_timestamp") val donateTimestamp: Long = 0L,
    @Json(name = "donate_date") val donateDate: String? = null,
    @Json(name = "donate_remark") val donateRemake: String? = null,
)

class DonateListRepository {

    suspend fun getDonateList(): List<DonateInfo> {
        return Api.getDonateList()
    }
}
