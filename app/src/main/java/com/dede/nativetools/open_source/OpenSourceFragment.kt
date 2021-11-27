package com.dede.nativetools.open_source

import android.graphics.Rect
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
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
        val list = loadOpenSource()
        binding.recyclerView.adapter = Adapter(list)
        binding.recyclerView.addItemDecoration(ItemDecoration())
        ItemTouchHelper(ItemTouchHelperCallback(list)).attachToRecyclerView(binding.recyclerView)
    }

    private class ItemTouchHelperCallback(private val list: MutableList<OpenSource>) :
        ItemTouchHelper.Callback() {
        override fun getMovementFlags(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder
        ): Int {
            return makeMovementFlags(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0)
        }

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            val from = viewHolder.bindingAdapterPosition
            val to = target.absoluteAdapterPosition
            list.add(to, list.removeAt(from))
            recyclerView.adapter?.notifyItemMoved(from, to)
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        }
    }

    private class ItemDecoration : RecyclerView.ItemDecoration() {
        private val offset = 12.dp

        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            val position = parent.getChildAdapterPosition(view)
            outRect.set(offset, if (position == 0) offset else 0, offset, offset)
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

            binding.ivMenu.setOnClickListener {
                showMenu(it, openSource)
            }
            itemView.setOnClickListener {
                val url = openSource.url
                if (url.isNotEmpty()) {
                    it.context.browse(url)
                }
            }
        }

        private fun showMenu(view: View, openSource: OpenSource) {
            val context = view.context
            val popupMenu = PopupMenu(context, view, Gravity.END)
            popupMenu.inflate(R.menu.menu_open_source)
            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_copy -> {
                        val url = openSource.url
                        if (url.isNotEmpty()) {
                            context.copy(url)
                            context.toast(R.string.toast_copyed)
                        }
                    }
                    R.id.action_open -> {
                        val url = openSource.url
                        if (url.isNotEmpty()) {
                            context.browse(url)
                        }
                    }
                }
                return@setOnMenuItemClickListener true
            }
            popupMenu.show()
        }
    }

    private class OpenSource(
        val name: String,
        val author: String?,
        val desc: String,
        val url: String?,
        val foregroundLogo: Int
    )

    private fun loadOpenSource(): MutableList<OpenSource> {
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
                R.drawable.ic_logo_github
            ),
            OpenSource(
                "ViewBindingPropertyDelegate",
                "kirich1409",
                "Make work with Android View Binding simpler.",
                "https://github.com/kirich1409/ViewBindingPropertyDelegate",
                R.drawable.ic_logo_github
            )
        )
    }
}
