package com.dede.nativetools.netspeed.typeface

import android.content.Context
import android.graphics.Typeface
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.work.*
import com.dede.nativetools.util.Logic
import com.dede.nativetools.util.closeFinally
import com.dede.nativetools.util.isEmpty
import com.google.firebase.ktx.Firebase
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.ktx.performance
import com.google.firebase.perf.ktx.trace
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

abstract class DownloadTypeface(val context: Context) : TypefaceGetter {

    companion object {

        fun create(context: Context, key: String): DownloadTypeface? {
            return TypefaceGetter.create(context, key) as? DownloadTypeface
        }

        fun getFontFile(context: Context, fontName: String): File {
            return File(File(context.filesDir, "fonts"), fontName)
        }

        fun loadFont(context: Context, fontName: String): Typeface? {
            val fontFile = getFontFile(context, fontName)
            return fontFile.runCatching(Typeface::createFromFile)
                .onFailure(Throwable::printStackTrace)
                .getOrNull()
        }
    }

    private var basic: Typeface? = null

    override fun canApply(): Boolean {
        return loadFont() != null
    }

    private fun loadFont(): Typeface? {
        var typeface = this.basic
        if (typeface == null) {
            typeface = loadFont(context, fontName).apply { basic = this }
        }
        return typeface
    }

    abstract val downloadUrl: String

    abstract val fontName: String

    override fun get(style: Int): Typeface {
        val typeface = loadFont()
            ?: return TypefaceGetter.getOrDefault(TypefaceGetter.FONT_NORMAL, style)
        return applyStyle(typeface, style)
    }

    open fun applyStyle(typeface: Typeface, style: Int): Typeface {
        return TypefaceGetter.applyStyle(typeface, style)
    }
}

open class DownloadTypefaceImpl(context: Context, override val fontName: String) :
    DownloadTypeface(context) {

    override val downloadUrl: String
        get() {
            return if (Logic.isSimplifiedChinese(context)) {
                // 大陆访问 gitee 仓库
                "https://gitee.com/dede_hu/fonts/raw/master/$fontName"
            } else {
                "https://github.com/hushenghao/fonts/raw/master/$fontName"
            }
        }
}

class DownloadFontWork(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {

    companion object {
        const val EXTRA_FONT_KEY = "extra_font_key"
        const val EXTRA_FONT_URL = "extra_font_url"
        const val EXTRA_FONT_NAME = "extra_font_name"

        fun downloadFont(context: Context, fontKey: String): LiveData<WorkInfo>? {
            val getter = DownloadTypeface.create(context, fontKey) ?: return null
            val data = workDataOf(
                EXTRA_FONT_KEY to fontKey,
                EXTRA_FONT_NAME to getter.fontName,
                EXTRA_FONT_URL to getter.downloadUrl
            )
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val work = OneTimeWorkRequestBuilder<DownloadFontWork>()
                .setInputData(data)
                .setConstraints(constraints)
                .build()
            val workManager = WorkManager.getInstance(context)
            workManager.beginWith(work)
                .enqueue()
            return workManager.getWorkInfoByIdLiveData(work.id)
        }
    }

    override suspend fun doWork(): Result {
        val context = applicationContext
        val fontKey = inputData.getString(EXTRA_FONT_KEY)
        val downloadUrl = inputData.getString(EXTRA_FONT_URL)
        val fontName = inputData.getString(EXTRA_FONT_NAME)
        if (downloadUrl.isEmpty() || fontName.isEmpty()) {
            // 没有下载参数配置
            return Result.failure()
        }
        return withContext(Dispatchers.IO) {
            Log.i("DownloadFontWork", "download: $downloadUrl")
            var result = DownloadTypeface.loadFont(context, fontName)
            if (result != null) {
                // 已下载
                return@withContext Result.success(workDataOf(EXTRA_FONT_KEY to fontKey))
            }
            // 开始下载
            val fontFile = DownloadTypeface.getFontFile(context, fontName)
            val dir = fontFile.parentFile
            if (dir != null && !dir.exists()) {
                dir.mkdirs()
            }
            download(downloadUrl, fontFile)
            // 检查下载结果
            result = DownloadTypeface.loadFont(context, fontName)
            return@withContext if (result != null)
                Result.success(workDataOf(EXTRA_FONT_KEY to fontKey))
            else
                Result.failure()
        }
    }

    /**
     * 下载网络字体
     */
    private fun download(urlStr: String, output: File) {
        val url = URL(urlStr)
        Firebase.performance.newHttpMetric(url.host, FirebasePerformance.HttpMethod.GET)
            .trace {
                var connect: HttpURLConnection? = null
                var outputStream: OutputStream? = null
                var inputStream: InputStream? = null
                try {
                    connect = (url.openConnection() as? HttpURLConnection) ?: return
                    connect.requestMethod = "GET"
                    connect.connectTimeout = 10000
                    connect.readTimeout = 10000
                    connect.doOutput = true
                    connect.connect()
                    val responseCode = connect.responseCode
                    setHttpResponseCode(responseCode)
                    if (responseCode == 200) {
                        inputStream = connect.inputStream
                        outputStream = FileOutputStream(output)
                        inputStream.copyTo(outputStream)
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                } finally {
                    outputStream?.closeFinally()
                    inputStream?.closeFinally()
                    connect?.disconnect()
                }
            }
    }
}
