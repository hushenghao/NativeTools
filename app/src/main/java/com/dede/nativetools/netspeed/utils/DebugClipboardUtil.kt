package com.dede.nativetools.netspeed.utils

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.dede.nativetools.util.*

class DebugClipboardUtil : BroadcastReceiver() {

    companion object {
        private const val TAG = "DebugClipboardReceiver"
        private const val ACTION_CLIPBOARD_GET = "clipboard.get"
        private const val ACTION_CLIPBOARD_SET = "clipboard.set"
        private const val EXTRA_TEXT = "text"
        private const val EXTRA_BASE64 = "base64"

        private val debugClipboardUtil by lazy { DebugClipboardUtil() }

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