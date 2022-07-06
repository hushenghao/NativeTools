package com.dede.nativetools.network


import com.dede.nativetools.donate.DonateInfo
import com.dede.nativetools.open_source.OpenSource
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Path
import java.io.InputStream

/**
 * Retrofit service class
 *
 * Created by shhu on 2022/6/27 15:34.
 * @since 2022/6/27
 */
interface Api {

    // The default instance of [Api] with Retrofit proxy.
    companion object Default : Api by proxy

    @GET("fonts/-/raw/master/{fontName}")
    suspend fun downloadFont(@Path("fontName") fontName: String): InputStream

    //@GET("http://10.103.0.157:8000/open_source_list.json")
    @GET("NativeTools/-/raw/develop/apis/open_source_list.json")
    suspend fun getOpenSourceList(): List<OpenSource>

    //@GET("http://10.103.0.157:8000/donate_list.json")
    @GET("NativeTools/-/raw/develop/apis/donate_list.json")
    suspend fun getDonateList(): List<DonateInfo>

}

private val loadingObj by lazy { Any() }

fun <T> Result.Companion.loading(): Result<T> {
    @Suppress("UNCHECKED_CAST")
    return success(loadingObj) as Result<T>
}

val Result<*>.isLoading: Boolean
    get() = this.getOrNull() == loadingObj


private val proxy by lazy {
    val retrofit = Retrofit.Builder()
        .baseUrl("https://gitlab.com/hushenghao/")
        .addConverterFactory(JsonConverterFactory.create())
        .addConverterFactory(StreamConverterFactory.create())
        .build()
    retrofit.create(Api::class.java)
}
