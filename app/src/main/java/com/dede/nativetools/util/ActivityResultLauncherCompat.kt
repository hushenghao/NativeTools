package com.dede.nativetools.util

import android.util.Log
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

/**
 * ActivityResultLaucher ext
 *
 * @author hsh
 * @since 2021/11/9 3:19 下午
 */
class ActivityResultLauncherCompat<I, O> constructor(
    private val caller: ActivityResultCaller,
    private val contract: ActivityResultContract<I, O>,
    private val registry: ActivityResultRegistry?,
    private val lifecycleOwner: LifecycleOwner
) : ActivityResultLauncher<I>(), DefaultLifecycleObserver, ActivityResultCallback<O> {

    private var activityResultLauncher: ActivityResultLauncher<I>? = null
    var activityResultCallback: ActivityResultCallback<O>? = null

    constructor(
        caller: ActivityResultCaller,
        contract: ActivityResultContract<I, O>,
        lifecycleOwner: LifecycleOwner
    ) : this(caller, contract, null, lifecycleOwner)

    constructor(fragment: Fragment, contract: ActivityResultContract<I, O>) :
            this(fragment, contract, fragment)

    constructor(activity: FragmentActivity, contract: ActivityResultContract<I, O>) :
            this(activity, contract, activity)

    init {
        lifecycleOwner.lifecycle.addObserver(this)
    }

    override fun onCreate(owner: LifecycleOwner) {
        activityResultLauncher = if (registry == null) {
            caller.registerForActivityResult(contract, this)
        } else {
            caller.registerForActivityResult(contract, registry, this)
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        if (activityResultLauncher == null) {
            throw IllegalStateException("ActivityResultLauncherCompat must initialize before they are STARTED.")
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        lifecycleOwner.lifecycle.removeObserver(this)
    }

    override fun onActivityResult(result: O) {
        activityResultCallback?.onActivityResult(result)
    }

    fun launch(input: I, callback: ActivityResultCallback<O>) {
        activityResultCallback = callback
        launch(input)
    }

    fun launch(input: I, options: ActivityOptionsCompat?, callback: ActivityResultCallback<O>) {
        activityResultCallback = callback
        launch(input, options)
    }

    override fun launch(input: I, options: ActivityOptionsCompat?) {
        if (activityResultCallback == null) {
            Log.w("ActivityResultLauncherCompat", "launch: activityResultCallback is null")
        }
        activityResultLauncher?.launch(input, options)
    }

    override fun unregister() {
    }

    override fun getContract(): ActivityResultContract<I, *> {
        return contract
    }
}