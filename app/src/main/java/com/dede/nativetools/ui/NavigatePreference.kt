package com.dede.nativetools.ui

import android.content.Context
import android.content.ContextWrapper
import android.util.AttributeSet
import androidx.annotation.IdRes
import androidx.preference.Preference
import com.dede.nativetools.R
import com.dede.nativetools.util.browse
import com.dede.nativetools.util.event
import com.dede.nativetools.util.isNotEmpty
import com.google.firebase.analytics.FirebaseAnalytics

class NavigatePreference(context: Context, attrs: AttributeSet) : Preference(context, attrs) {

    interface OnNavigateHandler {
        fun handleNavigate(@IdRes id: Int)
    }

    private val navigateId: Int
    private val navigateUrl: String?
    private val eventName: String?

    init {
        val typedArray =
            context.obtainStyledAttributes(attrs, R.styleable.NavigatePreference)
        navigateId = typedArray.getResourceId(R.styleable.NavigatePreference_navigateId, -1)
        navigateUrl = typedArray.getString(R.styleable.NavigatePreference_navigateUrl)
        eventName = typedArray.getString(R.styleable.NavigatePreference_eventName)
        typedArray.recycle()
        isPersistent = false
    }

    override fun onClick() {
        if (eventName.isNotEmpty()) {
            event(FirebaseAnalytics.Event.SELECT_ITEM) {
                param(FirebaseAnalytics.Param.ITEM_NAME, eventName)
                param(FirebaseAnalytics.Param.CONTENT_TYPE, "路由")
            }
        }
        if (navigateUrl.isNotEmpty()) {
            context.browse(navigateUrl)
            return
        }
        if (navigateId == -1) return
        (resolveContext(context) as? OnNavigateHandler)?.handleNavigate(navigateId)
    }

    private fun resolveContext(context: Context): Context? {
        var ctx: Context? = context
        while (ctx != null) {
            if (!ctx.isRestricted) {
                return ctx
            }
            ctx = if (ctx is ContextWrapper) {
                ctx.baseContext
            } else {
                null
            }
        }
        return null
    }
}