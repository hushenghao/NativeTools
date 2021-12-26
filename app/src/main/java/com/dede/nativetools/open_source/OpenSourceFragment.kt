package com.dede.nativetools.open_source

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.dede.nativetools.R
import com.dede.nativetools.databinding.FragmentOpenSourceBinding
import com.dede.nativetools.databinding.ItemOpenSourceBinding
import com.dede.nativetools.ui.SpaceItemDecoration
import com.dede.nativetools.util.*

/**
 * 开源相关
 */
class OpenSourceFragment : Fragment(R.layout.fragment_open_source) {

    private val binding by viewBinding(FragmentOpenSourceBinding::bind)
    private val viewModel by viewModels<OpenSourceViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = Adapter()
        binding.recyclerView.adapter = adapter
        binding.recyclerView.addItemDecoration(SpaceItemDecoration(12.dp))
        val itemTouchSwapCallback = ItemTouchSwapCallback(adapter::onSwap)
        val itemTouchHelper = ItemTouchHelper(itemTouchSwapCallback)
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)

        viewModel.openSourceList.observe(this, adapter::setData)
    }

    private class ItemTouchSwapCallback(private val onSwap: (from: Int, to: Int) -> Boolean) :
        ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            val from = viewHolder.bindingAdapterPosition
            val to = target.bindingAdapterPosition
            return onSwap(from, to)
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        }
    }

    private class Adapter : RecyclerView.Adapter<ViewHolder>() {

        private val list = mutableListOf<OpenSource>()

        fun setData(data: List<OpenSource>) {
            val start = this.list.size
            this.list.addAll(data)
            notifyItemRangeInserted(start, list.size - 1)
        }

        fun onSwap(from: Int, to: Int): Boolean {
            val range = 0 until list.size
            if (from !in range || to !in range)
                return false
            list.add(to, list.removeAt(from))
            notifyItemMoved(from, to)
            return true
        }

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

        fun bindViewData(openSource: OpenSource) {
            itemView.context.requireDrawable<Drawable>(openSource.foregroundLogo).apply {
                setBounds(0, 0, 18.dp, 18.dp)
                binding.tvProjectName.setCompoundDrawables(this, null, null, null)
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
                        if (openSource.url.isNotEmpty()) {
                            context.copy(openSource.url)
                            context.toast(R.string.toast_copyed)
                        }
                    }
                    R.id.action_open -> {
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
