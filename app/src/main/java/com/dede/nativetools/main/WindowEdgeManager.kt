/*
 * Copyright 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dede.nativetools.main

import android.content.Context
import android.graphics.Color
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.view.View
import android.view.Window
import androidx.core.graphics.ColorUtils
import com.google.android.material.color.MaterialColors

/**
 * Helper that saves the current window preferences for the Catalog.
 */
class WindowEdgeManager(private val context: Context) {

    fun applyEdgeToEdge(window: Window?) {
        if (window == null) return

        val statusBarColor = getStatusBarColor()
        val navbarColor = getNavBarColor()
        val lightBackground = MaterialColors.isColorLight(
            MaterialColors.getColor(
                context, android.R.attr.colorBackground, Color.BLACK
            )
        )
//        val lightStatusBar = MaterialColors.isColorLight(statusBarColor)
        val showDarkStatusBarIcons = false
//            lightStatusBar || statusBarColor == Color.TRANSPARENT && lightBackground
        val lightNavbar = MaterialColors.isColorLight(navbarColor)
        val showDarkNavbarIcons = lightNavbar || navbarColor == Color.TRANSPARENT && lightBackground
        val decorView = window.decorView
        val currentStatusBar = if (showDarkStatusBarIcons)
            @Suppress("DEPRECATION") View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR else 0
        val currentNavBar = if (showDarkNavbarIcons && VERSION.SDK_INT >= VERSION_CODES.O)
            @Suppress("DEPRECATION") View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR else 0
        window.navigationBarColor = navbarColor
        window.statusBarColor = statusBarColor
        val systemUiVisibility = (EDGE_TO_EDGE_FLAGS or currentStatusBar or currentNavBar)
        @Suppress("DEPRECATION")
        decorView.systemUiVisibility = systemUiVisibility
    }

    private fun getStatusBarColor(): Int {
        return Color.TRANSPARENT
    }

    private fun getNavBarColor(): Int {
        if (VERSION.SDK_INT < VERSION_CODES.O_MR1) {
            val opaqueNavBarColor =
                MaterialColors.getColor(context, android.R.attr.navigationBarColor, Color.BLACK)
            return ColorUtils.setAlphaComponent(opaqueNavBarColor, EDGE_TO_EDGE_BAR_ALPHA)
        }
        return Color.TRANSPARENT
    }

    companion object {
        private const val EDGE_TO_EDGE_BAR_ALPHA = 128

        @Suppress("DEPRECATION")
        private const val EDGE_TO_EDGE_FLAGS =
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
    }
}