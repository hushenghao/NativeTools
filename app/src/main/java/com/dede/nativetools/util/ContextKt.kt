@file:JvmName("ContextKt")

package com.dede.nativetools.util

import android.app.AppOpsManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Process
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import com.dede.nativetools.BuildConfig
import com.dede.nativetools.NativeToolsApp
import com.dede.nativetools.R
import java.io.InputStream
import kotlin.properties.ReadOnlyProperty


val globalContext: Context
    get() = NativeToolsApp.getInstance()

inline fun <reified T : Any> Context.requireSystemService(): T {
    return checkNotNull(applicationContext.getSystemService())
}

inline fun <reified T : Any> Context.systemService(): ReadOnlyProperty<Context, T> {
    return ReadOnlyProperty { _, _ -> requireSystemService() }
}

fun Context.launchActivity(intent: Intent) {
    intent.runCatching(this::startActivity).onFailure(Throwable::printStackTrace)
}

fun Context.startService(intent: Intent, foreground: Boolean) {
    if (foreground) {
        ContextCompat.startForegroundService(this, intent)
    } else {
        this.startService(intent)
    }
}

fun Context.checkAppOps(): Boolean {
    val appOpsManager = requireSystemService<AppOpsManager>()
    val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        appOpsManager.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            this.packageName
        )
    } else {
        appOpsManager.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            this.packageName
        )
    }
    return result == AppOpsManager.MODE_ALLOWED
}

fun Context.assets(fileName: String): InputStream {
    return assets.open(fileName)
}

fun <T : Drawable> Context.requireDrawable(@DrawableRes drawableId: Int): T {
    return checkNotNull(AppCompatResources.getDrawable(this, drawableId) as T)
}

fun Context.toast(text: String) {
    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
}

fun Context.toast(@StringRes resId: Int) {
    Toast.makeText(this, resId, Toast.LENGTH_SHORT).show()
}

fun Context.browse(url: String) {
    ChromeTabsBrowser.launchUrl(this, Uri.parse(url))
}

fun Context.browse(@StringRes urlId: Int) {
    this.browse(this.getString(urlId))
}

fun Context.market(packageName: String) {
    val market = Intent(Intent.ACTION_VIEW)
        .setData("market://details?id=$packageName")
        .newTask()
        .toChooser(R.string.chooser_label_market)
    startActivity(market)
}

fun Context.share(@StringRes textId: Int) {
    val intent = Intent(Intent.ACTION_SEND, Intent.EXTRA_TEXT to getString(textId))
        .setType("text/plain")
        .newTask()
        .toChooser(R.string.action_share)
    startActivity(intent)
}

fun Context.emailTo(@StringRes addressRes: Int) {
    val uri = Uri.parse("mailto:${getString(addressRes)}")
    val intent = Intent(Intent(Intent.ACTION_SENDTO, uri))
        .newTask()
        .toChooser(R.string.action_feedback)
    startActivity(intent)
}

fun Context.copy(text: String) {
    val clipboardManager = this.requireSystemService<ClipboardManager>()
    clipboardManager.setPrimaryClip(ClipData.newPlainText("text", text))
}

fun Context.readClipboard(): String? {
    val clipboardManager = this.requireSystemService<ClipboardManager>()
    val primaryClip = clipboardManager.primaryClip ?: return null
    if (primaryClip.itemCount > 0) {
        return primaryClip.getItemAt(0)?.text?.toString()
    }
    return null
}

fun Context.getVersionSummary() =
    getString(R.string.summary_about_version, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE)