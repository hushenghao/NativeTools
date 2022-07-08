package com.dede.nativetools.network

import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.io.InputStream
import java.lang.reflect.Type

/**
 * Created by shhu on 2022/6/30 09:42.
 *
 * @since 2022/6/30
 */
class StreamConverterFactory private constructor() : Converter.Factory() {

    companion object {
        fun create(): StreamConverterFactory {
            return StreamConverterFactory()
        }
    }

    override fun responseBodyConverter(
        type: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit,
    ): Converter<ResponseBody, InputStream>? {
        if (type != InputStream::class.java) return null

        return StreamResponseBodyConverter()
    }
}

private class StreamResponseBodyConverter : Converter<ResponseBody, InputStream> {
    override fun convert(value: ResponseBody): InputStream {
        return value.byteStream()
    }
}