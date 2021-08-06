package com.dede.nativetools.ui

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
import com.dede.nativetools.util.browse
import com.dede.nativetools.util.isEmpty

/**
 * 开源相关
 */
class OpenSourceFragment : Fragment(R.layout.fragment_open_source) {

    private val binding by viewBinding(FragmentOpenSourceBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerView.adapter = Adapter(loadOpenSource())
    }

    private class Adapter(val list: List<OpenSource>) : RecyclerView.Adapter<ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_open_source, parent, false)
            return ViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val openSource = list[position]
            holder.bindViewData(openSource)
        }

        override fun getItemCount(): Int {
            return list.size
        }
    }

    private class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = ItemOpenSourceBinding.bind(view)

        fun bindViewData(openSource: OpenSource) {
            binding.tvProjectName.text = openSource.name
            binding.tvAuthorName.text = openSource.author
            binding.tvProjectDesc.text = openSource.desc
            itemView.setOnClickListener {
                val url = openSource.url
                if (url == null || url.isEmpty) return@setOnClickListener
                it.context.browse(url)
            }
        }
    }

    private class OpenSource(
        val name: String,
        val author: String?,
        val desc: String,
        val url: String?
    )

    private fun loadOpenSource(): List<OpenSource> {
        return arrayListOf(
            OpenSource(
                "Kotlin",
                "JetBrains",
                "Write better Android apps faster with Kotlin.",
                "https://developer.android.google.cn/kotlin"
            ),
            OpenSource(
                "Jetpack",
                "Google",
                "Jetpack is a suite of libraries to help developers follow best practices, reduce boilerplate code, and write code that works consistently across Android versions and devices so that developers can focus on the code they care about.",
                "https://developer.android.google.cn/jetpack"
            ),
            OpenSource(
                "Material Design",
                "Google",
                "Material is a design system – backed by open-source code – that helps teams build high-quality digital experiences.",
                "https://material.io/"
            ),
            OpenSource(
                "FreeReflection",
                "tiann",
                "FreeReflection is a library that lets you use reflection without any restriction above Android P (includes Q and R).",
                "https://github.com/tiann/FreeReflection"
            ),
            OpenSource(
                "ViewBindingPropertyDelegate",
                "kirich1409",
                "Make work with Android View Binding simpler.",
                "https://github.com/kirich1409/ViewBindingPropertyDelegate"
            )
        )
    }
}
