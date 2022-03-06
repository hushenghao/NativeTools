package com.dede.nativetools.open_source

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isGone
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dede.nativetools.R
import com.dede.nativetools.databinding.ItemOpenSourceBinding
import com.dede.nativetools.ui.AbsBottomSheetListFragment
import com.dede.nativetools.ui.GridItemDecoration
import com.dede.nativetools.util.*

/**
 * 开源相关
 */
class OpenSourceFragment : AbsBottomSheetListFragment<OpenSource, OpenSourceFragment.ViewHolder>() {

    private val viewModel by viewModels<OpenSourceViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvMessage.isGone = true
        binding.tvTitle.isGone = true
        binding.recyclerView.addItemDecoration(GridItemDecoration(12.dp))
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        viewModel.openSourceList.observe(this) {
            setData(it)
        }
        // "^(META-INF/)?([^/]+)\\.(version|properties)$"
    }

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_open_source, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, t: OpenSource) {
        holder.bindViewData(t)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val binding = ItemOpenSourceBinding.bind(view)

        fun bindViewData(openSource: OpenSource) {
            val drawable =
                itemView.context.requireDrawable<Drawable>(openSource.foregroundLogo, 18.dp)
            binding.tvProjectName.setCompoundDrawablesRelative(start = drawable)
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
            if (openSource.license.isEmpty()) {
                popupMenu.menu.findItem(R.id.action_license).isEnabled = false
            }
            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_copy -> {
                        if (openSource.url.isNotEmpty()) {
                            context.copy(openSource.url)
                        }
                    }
                    R.id.action_homepage -> {
                        if (openSource.url.isNotEmpty()) {
                            context.browse(openSource.url)
                        }
                    }
                    R.id.action_license -> {
                        if (openSource.license.isNotEmpty()) {
                            context.browse(openSource.license)
                        }
                    }
                }
                return@setOnMenuItemClickListener true
            }
            popupMenu.show()
        }
    }
}
