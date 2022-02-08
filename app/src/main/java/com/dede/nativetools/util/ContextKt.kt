@file:JvmName("ContextKt")

package com.dede.nativetools.util

import android.content.*
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.IBinder
import android.widget.Toast
import androidx.annotation.*
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.dede.nativetools.NativeToolsApp
import com.dede.nativetools.R
import com.google.android.material.color.MaterialColors
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

typealias OnServiceConnected = (service: IBinder?) -> Unit

fun Context.bindService(
    intent: Intent,
    onConnected: OnServiceConnected,
    onFailed: () -> Unit,
    lifecycleOwner: LifecycleOwner,
): Boolean {
    val conn = LifecycleServiceConnection(this, onConnected)
    val bind = this.bindService(intent, conn, Context.BIND_AUTO_CREATE)
    if (!bind) {
        onFailed.invoke()
    }
    lifecycleOwner.lifecycle.addObserver(conn)
    return bind
}

private class LifecycleServiceConnection(
    val context: Context,
    val onConnected: OnServiceConnected,
) : ServiceConnection, DefaultLifecycleObserver {

    override fun onDestroy(owner: LifecycleOwner) {
        context.unbindService(this)
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        onConnected.invoke(service)
    }

    override fun onServiceDisconnected(name: ComponentName?) {
    }
}

fun Context.assets(fileName: String): InputStream {
    return assets.open(fileName)
}

@Suppress("UNCHECKED_CAST")
fun <T : Drawable> Context.requireDrawable(@DrawableRes drawableId: Int): T {
    return checkNotNull(AppCompatResources.getDrawable(this, drawableId) as T)
}

@ColorInt
fun Context.color(@ColorRes colorId: Int): Int {
    return ContextCompat.getColor(this, colorId)
}

@ColorInt
fun Context.color(@AttrRes attrId: Int, @ColorInt default: Int): Int {
    return MaterialColors.getColor(this, attrId, default)
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
    ContextCompat.startActivity(this, market, null)
}

fun Context.share(@StringRes textId: Int) {
    val intent = Intent(Intent.ACTION_SEND, Intent.EXTRA_TEXT to getString(textId))
        .setType("text/plain")
        .newTask()
        .toChooser(R.string.action_share)
    ContextCompat.startActivity(this, intent, null)
}

fun Context.emailTo(@StringRes addressRes: Int) {
    val uri = Uri.parse("mailto:${getString(addressRes)}")
    val intent = Intent(Intent(Intent.ACTION_SENDTO, uri))
        .newTask()
        .toChooser(R.string.action_feedback)
    ContextCompat.startActivity(this, intent, null)
}

fun Context.copy(text: String) {
    val clipboardManager = this.requireSystemService<ClipboardManager>()
    clipboardManager.setPrimaryClip(ClipData.newPlainText("text", text))
}
