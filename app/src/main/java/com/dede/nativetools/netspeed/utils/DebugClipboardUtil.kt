package com.dede.nativetools.netspeed.utils

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.dede.nativetools.util.*


/**
 * 添加了一个小的开发工具
 * 使用adb修改/获取剪切板内容
 *
 * 修改剪切板
 * adb shell am broadcast -a clipboard.set -e text "Hello World!"
 * adb shell am broadcast -a clipboard.set -e base64 "SGVsbG8gV29ybGQh"
 *
 * 读取剪切板 (由于系统限制，只有应用在前台才能读取)
 * adb shell am broadcast -a clipboard.get
 *
 * Why?
 * adb shell input text "Hello World!"
 * 对输入内容做了限制，同时对ime也有要求，所以这里使用剪切板来快捷发送字符串到手机
 * 由于NetSpeedService做为前台服务，可长时间稳定运行，所以直接依附于当前服务
 */
class DebugClipboardUtil : BroadcastReceiver() {

    companion object {
        private const val TAG = "DebugClipboardReceiver"
        private const val ACTION_CLIPBOARD_GET = "clipboard.get"
        private const val ACTION_CLIPBOARD_SET = "clipboard.set"
        private const val EXTRA_TEXT = "text"
        private const val EXTRA_BASE64 = "base64"

        private val debugClipboardUtil = DebugClipboardUtil()

        fun register(context: Context) {
            val clipboardIntentFilter = IntentFilter()
                .addActions(ACTION_CLIPBOARD_GET, ACTION_CLIPBOARD_SET)
            context.registerReceiver(debugClipboardUtil, clipboardIntentFilter)
        }

        fun unregister(context: Context) {
            context.unregisterReceiver(debugClipboardUtil)
        }
    }

    private fun getClipboard(context: Context) {
        val text = context.readClipboard()
        if (text != null) {
            setResult(Activity.RESULT_OK, text)
        } else {
            setResult(Activity.RESULT_CANCELED, "get clipboard failure")
        }
    }

    private fun setClipboard(context: Context, intent: Intent) {
        var result: String? = null
        val text = intent.getStringExtra(EXTRA_TEXT)
        val base64 = intent.getStringExtra(EXTRA_BASE64)
        if (text != null && text.isNotEmpty) {
            result = text
        } else if (base64 != null && base64.isNotEmpty) {
            result = base64.decodeBase64()
        }
        if (result != null && result.isNotEmpty) {
            context.copy(result)
            setResult(Activity.RESULT_OK, "set clipboard ok")
        } else {
            setResult(Activity.RESULT_CANCELED, "set clipboard failure")
        }
    }

    private fun setResult(code: Int, data: String) {
        resultCode = code
        resultData = data
    }

    override fun onReceive(context: Context, intent: Intent?) {
        when (intent?.action) {
            ACTION_CLIPBOARD_GET -> {
                getClipboard(context) // 只有应用在前台时才可以获取到
            }
            ACTION_CLIPBOARD_SET -> {
                setClipboard(context, intent)
            }
        }
    }
}