package com.dede.nativetools.donate

import com.dede.nativetools.network.Api
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Created by shhu on 2022/7/1 13:43.
 *
 * @since 2022/7/1
 */
@Serializable
data class DonateInfo(
    @SerialName("order_id")
    val orderId: String? = null,
    @SerialName("donate_price")
    val donatePrice: String? = null,
    @SerialName("donate_channel")
    val donateChannel: String? = null,
    @SerialName("donate_user")
    val donateUser: String? = null,
    @SerialName("donate_timestamp")
    val donateTimestamp: Long = 0L,
    @SerialName("donate_date")
    val donateDate: String? = null,
    @SerialName("donate_remark")
    val donateRemake: String? = null,
)

class DonateListRepository {

    suspend fun getDonateList(): List<DonateInfo> {
        return Api.getDonateList()
    }
}