package com.dede.nativetools.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.browser.customtabs.*
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.perf.metrics.AddTrace

/**
 * CustomTabs Help
 *
 * @author hsh
 * @since 2021/11/19 2:14 下午
 */
object ChromeTabsBrowser {

    // Package name for the Chrome channel the client wants to connect to. This depends on the
    // channel name.
    // Stable = com.android.chrome
    // Beta = com.chrome.beta
    // Dev = com.chrome.dev
    private const val CUSTOM_TAB_PACKAGE_NAME = "com.android.chrome"
    private const val CUSTOM_SESSION_ID = 10

    private var mayLaunchUrls: Array<out Uri>? = null
    private val customTabsCallback = CustomTabsCallback()
    private var customTabsSession: CustomTabsSession? = null

    private val customTabsServiceConnection =
        object : CustomTabsServiceConnection() {
            override fun onServiceDisconnected(name: ComponentName?) {
                customTabsSession = null
            }

            override fun onCustomTabsServiceConnected(
                name: ComponentName,
                client: CustomTabsClient
            ) {
                val result = client.warmup(0)
                if (!result) return
                val session = client.newSession(customTabsCallback, CUSTOM_SESSION_ID) ?: return

                customTabsSession = session
                val mayLaunchUrls = mayLaunchUrls ?: return
                mayLaunchUrls(session, mayLaunchUrls)
            }
        }

    private fun mayLaunchUrls(session: CustomTabsSession, mayLaunchUrls: Array<out Uri>) {
        val size = mayLaunchUrls.size
        if (size == 0) {
            return
        }
        val otherLikelyBundles =
            (1 until size)
                .map {
                    val bundle = Bundle()
                    bundle.putParcelable(CustomTabsService.KEY_URL, mayLaunchUrls[it])
                    bundle
                }
                .toList()
        session.mayLaunchUrl(mayLaunchUrls[0], null, otherLikelyBundles)
    }

    /** 预热并预加载 */
    @AddTrace(name = "CustomTabs.warmup")
    fun warmup(context: Context, vararg mayLaunchUrls: Uri): Boolean {
        if (customTabsSession != null) return true
        this.mayLaunchUrls = mayLaunchUrls
        val appContext = context.applicationContext
        return CustomTabsClient.bindCustomTabsService(
            appContext,
            CUSTOM_TAB_PACKAGE_NAME,
            customTabsServiceConnection
        )
    }

    @AddTrace(name = "CustomTabs.launch")
    fun launchUrl(context: Context, uri: Uri) {
        val colorScheme =
            if (isNightMode()) CustomTabsIntent.COLOR_SCHEME_DARK
            else CustomTabsIntent.COLOR_SCHEME_LIGHT
        val builder =
            CustomTabsIntent.Builder()
                .setColorScheme(colorScheme)
                .setShareState(CustomTabsIntent.SHARE_STATE_ON)
        val session = customTabsSession
        if (session != null) {
            builder.setSession(session)
        }
        val customTabsIntent = builder.build()
        customTabsIntent.intent.putExtra(
            Intent.EXTRA_REFERRER,
            Uri.parse("android-app://${context.packageName}")
        )
        try {
            customTabsIntent.launchUrl(context, uri)
        } catch (e: Exception) {
            Firebase.crashlytics.recordException(e)
        }
    }
}
