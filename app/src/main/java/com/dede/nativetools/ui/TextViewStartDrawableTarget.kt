package com.dede.nativetools.ui

import android.graphics.drawable.Drawable
import android.widget.TextView
import androidx.annotation.Dimension
import com.bumptech.glide.request.transition.Transition
import com.dede.nativetools.util.dp
import com.dede.nativetools.util.setCompoundDrawablesRelative

/**
 * Created by shhu on 2022/7/15 17:22.
 *
 * @since 2022/7/15
 */
@Suppress("DEPRECATION")
class TextViewStartDrawableTarget(
    view: TextView,
    @Dimension(unit = Dimension.DP) private val size: Float,
) : com.bumptech.glide.request.target.ViewTarget<TextView, Drawable>(view) {

    override fun onLoadFailed(errorDrawable: Drawable?) {
        errorDrawable.setBounds()
        view.setCompoundDrawablesRelative(start = errorDrawable)
    }

    override fun onLoadStarted(placeholder: Drawable?) {
        placeholder.setBounds()
        view.setCompoundDrawablesRelative(start = placeholder)
    }

    override fun onLoadCleared(placeholder: Drawable?) {
        placeholder.setBounds()
        view.setCompoundDrawablesRelative(start = placeholder)
    }

    override fun onResourceReady(
        resource: Drawable,
        transition: Transition<in Drawable>?,
    ) {
        resource.setBounds()
        view.setCompoundDrawablesRelative(start = resource)
    }

    private fun Drawable?.setBounds() {
        this?.setBounds(0, 0, size.dp, size.dp)
    }
}
