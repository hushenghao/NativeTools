package com.dede.nativetools.network

import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

/**
 * Created by shhu on 2022/6/29 10:26.
 *
 * Kotlin JSON decode for Retrofit
 *
 * @since 2022/6/29
 */
class JsonConverterFactory private constructor(private val json: Json) :
    Converter.Factory() {

    companion object {

        fun create(): JsonConverterFactory {
            return create(Json)
        }

        fun create(json: Json): JsonConverterFactory {
            return JsonConverterFactory(json)
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun responseBodyConverter(
        type: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit,
    ): Converter<ResponseBody, *>? {
        val kSerializer = json.serializersModule.serializerOrNull(type) ?: return null
        return JsonResponseBodyConverter(json, kSerializer)
    }

    override fun requestBodyConverter(
        type: Type,
        parameterAnnotations: Array<out Annotation>,
        methodAnnotations: Array<out Annotation>,
        retrofit: Retrofit,
    ): Converter<*, RequestBody> {
        return JsonRequestBodyConverter(json)
    }

}

private class JsonRequestBodyConverter(private val json: Json) : Converter<Any, RequestBody> {

    companion object {
        private val MEDIA_TYPE = MediaType.get("application/json; charset=UTF-8")
    }

    override fun convert(value: Any): RequestBody? {
        val json = json.encodeToString(value)
        return RequestBody.create(MEDIA_TYPE, json)
    }
}

private class JsonResponseBodyConverter(private val json: Json, private val kSerializer: KSerializer<Any>) :
    Converter<ResponseBody, Any> {

    @OptIn(ExperimentalSerializationApi::class)
    override fun convert(value: ResponseBody): Any {
        return value.use {
            json.decodeFromStream(kSerializer, it.byteStream())
        }
    }
}
