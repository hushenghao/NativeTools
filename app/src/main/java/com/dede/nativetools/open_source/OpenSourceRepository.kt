package com.dede.nativetools.open_source

import androidx.annotation.DrawableRes
import com.dede.nativetools.R

class OpenSource(
    val name: String,
    val author: String?,
    val desc: String,
    val url: String?,
    @DrawableRes
    val foregroundLogo: Int,
    val license: String?
)

class OpenSourceRepository {

    fun getOpenSourceList(): List<OpenSource> {
        return arrayListOf(
            OpenSource(
                "Kotlin",
                "JetBrains",
                "Write better Android apps faster with Kotlin.",
                "https://developer.android.google.cn/kotlin",
                R.drawable.insert_logo_kotlin_for_android,
                "https://www.apache.org/licenses/LICENSE-2.0.txt"
            ),
            OpenSource(
                "Kotlin",
                "JetBrains",
                "Write better Android apps faster with Kotlin.",
                "https://developer.android.google.cn/kotlin",
                R.drawable.insert_logo_kotlin_for_android,
                "https://www.apache.org/licenses/LICENSE-2.0.txt"
            ),
            OpenSource(
                "Kotlin",
                "JetBrains",
                "Write better Android apps faster with Kotlin.",
                "https://developer.android.google.cn/kotlin",
                R.drawable.insert_logo_kotlin_for_android,
                "https://www.apache.org/licenses/LICENSE-2.0.txt"
            ),
            OpenSource(
                "Jetpack",
                "Google",
                "Jetpack is a suite of libraries to help developers follow best practices, reduce boilerplate code, and write code that works consistently across Android versions and devices so that developers can focus on the code they care about.",
                "https://developer.android.google.cn/jetpack",
                R.drawable.ic_logo_jetpack,
                "https://www.apache.org/licenses/LICENSE-2.0.txt"
            ),
            OpenSource(
                "Material Design 3",
                "Google",
                "Make personal devices feel personal with Material Design 3, Google’s most expressive and adaptable design system yet. Coming first to Android 12.",
                "https://m3.material.io",
                R.drawable.ic_logo_material3,
                "https://www.apache.org/licenses/LICENSE-2.0.txt"
            ),
            OpenSource(
                "FreeReflection",
                "weishu",
                "FreeReflection is a library that lets you use reflection without any restriction above Android P (includes Q and R).",
                "https://github.com/tiann/FreeReflection",
                R.drawable.ic_logo_github,
                "https://raw.githubusercontent.com/tiann/FreeReflection/master/LICENSE"// (MIT License)
            ),
            OpenSource(
                "ViewBindingPropertyDelegate",
                "Android Broadcast",
                "Make work with Android View Binding simpler.",
                "https://github.com/androidbroadcast/ViewBindingPropertyDelegate",
                R.drawable.ic_logo_github,
                "https://www.apache.org/licenses/LICENSE-2.0.txt"
            ),
            OpenSource(
                "LeakCanary",
                "Square",
                "LeakCanary is a memory leak detection library for Android.",
                "https://square.github.io/leakcanary",
                R.drawable.ic_logo_leak_canary,
                "https://www.apache.org/licenses/LICENSE-2.0.txt"
            ),
            OpenSource(
                "Spotless",
                "DiffPlug",
                "Keep your code spotless.",
                "https://github.com/diffplug/spotless",
                R.drawable.ic_logo_spotless,
                "https://www.apache.org/licenses/LICENSE-2.0.txt"
            ),
            OpenSource(
                "Bebas Kai(改)",
                "Ryoichi Tsunekawa",
                "Bebas Kai(改) is a free display font for headline, caption, and titling designed by Ryoichi Tsunekawa.",
                "http://bebaskai.com",
                R.drawable.ic_logo_ryoichi_tsunekawa,
                "https://raw.githubusercontent.com/dharmatype/Bebas-Kai/master/OFL.txt"
            )
        )
    }
}