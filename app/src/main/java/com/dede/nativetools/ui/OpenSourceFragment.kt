package com.dede.nativetools.ui

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.dede.nativetools.R
import com.dede.nativetools.databinding.FragmentOpenSourceBinding
import com.dede.nativetools.databinding.ItemOpenSourceBinding
import com.dede.nativetools.util.*

/**
 * 开源相关
 */
class OpenSourceFragment : Fragment(R.layout.fragment_open_source) {

    private val binding by viewBinding(FragmentOpenSourceBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerView.adapter = Adapter(loadOpenSource())
        binding.recyclerView.addItemDecoration(ItemDecoration())
    }

    private class ItemDecoration : RecyclerView.ItemDecoration() {
        private val height = 12.dp

        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            val itemCount = parent.adapter?.itemCount ?: return
            val position = parent.getChildAdapterPosition(view)
            outRect.set(height, height, height, if (position >= itemCount - 1) height else 0)
        }
    }

    private class Adapter(val list: List<OpenSource>) : RecyclerView.Adapter<ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_open_source, parent, false)
            return ViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bindViewData(list[position])
        }

        override fun getItemCount(): Int {
            return list.size
        }
    }

    private class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val binding = ItemOpenSourceBinding.bind(view)
        private val ovalOutlineProvider = ViewOvalOutlineProvider(true)

        fun bindViewData(openSource: OpenSource) {
            binding.ivProjectLogo.apply {
                setImageResource(openSource.foregroundLogo)
                outlineProvider = ovalOutlineProvider
            }
            binding.tvProjectName.text = openSource.name
            binding.tvAuthorName.text = openSource.author
            binding.tvProjectDesc.text = openSource.desc

            val url = openSource.url
            if (url == null || url.isEmpty) {
                itemView.setOnClickListener(null)
                itemView.setOnLongClickListener(null)
            } else {
                itemView.setOnClickListener {
                    it.context.browse(url)
                }
                itemView.setOnLongClickListener {
                    it.context.apply {
                        copy(url)
                        toast(R.string.toast_copyed)
                    }
                    return@setOnLongClickListener true
                }
            }
        }
    }

    private class OpenSource(
        val name: String,
        val author: String?,
        val desc: String,
        val url: String?,
        val foregroundLogo: Int
    )

    private fun loadOpenSource(): List<OpenSource> {
        return arrayListOf(
            OpenSource(
                "Kotlin",
                "JetBrains",
                "Write better Android apps faster with Kotlin.",
                "https://developer.android.google.cn/kotlin",
                R.drawable.layer_logo_kotlin_for_android
            ),
            OpenSource(
                "Jetpack",
                "Google",
                "Jetpack is a suite of libraries to help developers follow best practices, reduce boilerplate code, and write code that works consistently across Android versions and devices so that developers can focus on the code they care about.",
                "https://developer.android.google.cn/jetpack",
                R.drawable.layer_logo_jetpack
            ),
            OpenSource(
                "Material Design",
                "Google",
                "Material is a design system – backed by open-source code – that helps teams build high-quality digital experiences.",
                "https://material.io/",
                R.drawable.layer_logo_material
            ),
            OpenSource(
                "FreeReflection",
                "tiann",
                "FreeReflection is a library that lets you use reflection without any restriction above Android P (includes Q and R).",
                "https://github.com/tiann/FreeReflection",
                R.drawable.layer_logo_github
            ),
            OpenSource(
                "ViewBindingPropertyDelegate",
                "kirich1409",
                "Make work with Android View Binding simpler.",
                "https://github.com/kirich1409/ViewBindingPropertyDelegate",
                R.drawable.layer_logo_github
            ),
            OpenSource(
                "Lottie",
                "airbnb",
                "Lottie is a mobile library for Android and iOS that parses Adobe After Effects animations exported as json with Bodymovin and renders them natively on mobile!",
                "https://github.com/airbnb/lottie-android",
                R.drawable.layer_logo_lottie
            )
        )
    }
}
