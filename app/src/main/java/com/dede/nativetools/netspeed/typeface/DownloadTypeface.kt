package com.dede.nativetools.netspeed.typeface

import android.content.Context
import android.graphics.Typeface
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.work.*
import com.dede.nativetools.util.isEmpty
import com.dede.nativetools.util.isSimplifiedChinese
import kotlinx.coroutines.*
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

abstract class DownloadTypeface(val context: Context) : TypefaceGetter {

    companion object {

        fun create(context: Context, key: String): DownloadTypeface? {
            return TypefaceGetter.create(context, key) as? DownloadTypeface
        }

        private fun getFontDir(context: Context): File {
            return File(context.filesDir, "fonts").apply {
                if (!exists()) {
                    mkdirs()
                }
            }
        }

        fun getFontFile(context: Context, fontName: String): File {
            return File(getFontDir(context), fontName)
        }

        @Throws(IOException::class)
        fun loadFont(context: Context, fontName: String): Typeface {
            val fontFile = getFontFile(context, fontName)
            return Typeface.createFromFile(fontFile)
        }

        fun checkFont(context: Context, fontName: String): Boolean {
            val fontFile = getFontFile(context, fontName)
            if (!fontFile.exists()) return false
            val typeface = fontFile.runCatching(Typeface::createFromFile)
                .onFailure(Throwable::printStackTrace)
                .getOrNull()
            return typeface != null && typeface != Typeface.DEFAULT
        }
    }

    private var basic: Typeface? = null

    private var canApplyCache = false

    override fun canApply(): Boolean {
        return checkFont(context, fontName).apply { canApplyCache = this }
    }

    abstract val downloadUrl: String

    abstract val fontName: String

    override fun get(style: Int): Typeface {
        var typeface = this.basic
        if (typeface == null) {
            typeface = loadFont(context, fontName).apply { basic = this }
        }
        if (canApplyCache) {
            // 内存缓存，减少io操作
            return applyStyle(typeface, style)
        }
        if (!canApply()) {
            return TypefaceGetter.getOrDefault(TypefaceGetter.FONT_NORMAL, style)
        }
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
            return if (isSimplifiedChinese(context)) {
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
            val work = OneTimeWorkRequestBuilder<DownloadFontWork>()
                .setInputMerger(OverwritingInputMerger::class)
                .setInputData(data)
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
            var result = DownloadTypeface.checkFont(context, fontName)
            if (result) {
                // 已下载
                return@withContext Result.success(workDataOf(EXTRA_FONT_KEY to fontKey))
            }
            // 开始下载
            val fontFile = DownloadTypeface.getFontFile(context, fontName)
            download(downloadUrl, fontFile)
            // 检查下载结果
            result = DownloadTypeface.checkFont(context, fontName)
            return@withContext if (result)
                Result.success(workDataOf(EXTRA_FONT_KEY to fontKey))
            else
                Result.failure()
        }
    }

    /**
     * 下载网络字体
     */
    private fun download(url: String, output: File) {
        var connect: HttpURLConnection? = null
        try {
            connect = (URL(url).openConnection() as? HttpURLConnection) ?: return
            connect.requestMethod = "GET"
            connect.connectTimeout = 15000
            connect.readTimeout = 15000
            connect.connect()
            if (connect.responseCode == 200) {
                connect.inputStream.use {
                    it.copyTo(output.outputStream())
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            connect?.disconnect()
        }
    }
}
