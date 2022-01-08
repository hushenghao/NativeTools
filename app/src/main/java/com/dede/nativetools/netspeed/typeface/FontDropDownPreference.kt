package com.dede.nativetools.netspeed.typeface

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.work.*
import com.dede.nativetools.ui.FreestyleDropDownPreference
import com.dede.nativetools.util.isEmpty
import com.dede.nativetools.util.toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/**
 * 自定义字体
 */
class FontDropDownPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = androidx.preference.R.attr.dropdownPreferenceStyle,
    defStyleRes: Int = 0
) : FreestyleDropDownPreference(context, attrs, defStyleAttr, defStyleRes), Observer<WorkInfo> {

    override fun freestyle(position: Int, textView: TextView) {
        val fontKey = entryValues[position].toString()
        val getter = TypefaceGetter.create(context, fontKey)
        if (!getter.canApply()) {
            return
        }
        textView.typeface = getter.get(Typeface.NORMAL)
    }

    init {
        super.setOnPreferenceChangeListener { _, newValue ->
            val fontKey = newValue.toString()
            val getter = TypefaceGetter.create(context, fontKey)
            val canApply = getter.canApply()
            if (!canApply) {
                // 下载字体
                downloadFont(fontKey)
                notifyChanged()// fix Spinner.OnItemSelectedListener cannot recall
            }
            return@setOnPreferenceChangeListener canApply// 不应用字体
        }
    }

    private var downloadLiveData: LiveData<WorkInfo>? = null

    private fun downloadFont(fontKey: String) {
        val getter = DownloadTypeface.create(context, fontKey) ?: return
        val data = workDataOf(
            DownloadFontWork.EXTRA_FONT_KEY to fontKey,
            DownloadFontWork.EXTRA_FONT_NAME to getter.fontName,
            DownloadFontWork.EXTRA_FONT_URL to getter.downloadUrl
        )
        val work = OneTimeWorkRequestBuilder<DownloadFontWork>()
            .setInputMerger(OverwritingInputMerger::class)
            .setInputData(data)
            .build()
        val workManager = WorkManager.getInstance(context)
        workManager.beginWith(work)
            .enqueue()
        downloadLiveData = workManager.getWorkInfoByIdLiveData(work.id)
            .also { it.observeForever(this) }
    }

    override fun onChanged(workInfo: WorkInfo) {
        when (workInfo.state) {
            WorkInfo.State.SUCCEEDED -> {
                context.toast(com.dede.nativetools.R.string.toast_downlad_font_succeeded)
            }
            WorkInfo.State.FAILED -> {
                context.toast(com.dede.nativetools.R.string.toast_downlad_font_failed)
            }
            WorkInfo.State.RUNNING -> {
                context.toast(com.dede.nativetools.R.string.toast_downlad_font)
            }
            else -> {

            }
        }
    }

    override fun onDetached() {
        downloadLiveData?.removeObserver(this)
        super.onDetached()
    }

    class DownloadFontWork(context: Context, workerParams: WorkerParameters) :
        CoroutineWorker(context, workerParams) {

        companion object {
            const val EXTRA_FONT_KEY = "extra_font_key"
            const val EXTRA_FONT_URL = "extra_font_url"
            const val EXTRA_FONT_NAME = "extra_font_name"
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
                var result = DownloadTypeface.checkFont(context, fontName)
                if (result) {
                    // 已下载
                    return@withContext Result.success()
                }
                // 开始下载
                val fontFile = DownloadTypeface.getFontFile(context, fontName)
                download(downloadUrl, fontFile)
                // 检查下载结果
                result = DownloadTypeface.checkFont(context, fontName)
                return@withContext if (result) Result.success() else Result.failure()
            }
        }

        /**
         * 下载网络字体
         */
        private fun download(url: String, output: File) {
            runBlocking {
                var connect: HttpURLConnection? = null
                try {
                    connect = (URL(url).openConnection() as? HttpURLConnection)
                    val http = connect ?: return@runBlocking
                    http.requestMethod = "GET"
                    http.connectTimeout = 15000
                    http.readTimeout = 15000
                    if (http.responseCode == 200) {
                        http.inputStream.use {
                            it.copyTo(output.outputStream())
                        }
                    }
                } finally {
                    connect?.disconnect()
                }
            }
        }
    }

    override fun setOnPreferenceChangeListener(onPreferenceChangeListener: OnPreferenceChangeListener?) {
        throw IllegalStateException("setOnPreferenceChangeListener method cannot be override")
    }
}