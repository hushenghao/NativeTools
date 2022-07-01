package com.dede.nativetools.open_source

import com.dede.nativetools.network.Api
import kotlinx.serialization.Serializable
import java.text.Collator

@Serializable
data class OpenSource(
    val name: String,
    val author: String?,
    val desc: String,
    val website: String?,
    val logo: String? = null,
    val license: String?,
    val icon: String? = null,
)

class OpenSourceRepository {

    suspend fun getOpenSourceList(): List<OpenSource> {
        val collator = Collator.getInstance()
        val list = Api.getOpenSourceList().toMutableList()
        list.sortWith { c1, c2 ->
            collator.compare(c1.name, c2.name)
        }
        return list
    }
}