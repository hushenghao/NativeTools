package com.dede.nativetools.ui

import android.app.Dialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.dede.nativetools.R
import com.dede.nativetools.databinding.FragmentBottomSheetListBinding
import com.dede.nativetools.databinding.ItemBottomSheetListBinding
import com.dede.nativetools.main.WindowEdgeManager
import com.dede.nativetools.util.isNightMode
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

abstract class BottomSheetListFragment<T> : BottomSheetDialogFragment() {

    protected val binding by viewBinding(FragmentBottomSheetListBinding::bind)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_bottom_sheet_list, container, false)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            WindowEdgeManager(requireContext()).applyEdgeToEdge(this.window)
            val behavior = (this as BottomSheetDialog).behavior
            behavior.skipCollapsed = true
        }
    }

    open fun setData(list: List<T>) {
        if (list.size < 4) {
            binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), list.size)
        }
        binding.recyclerView.adapter = Adapter(list, this)
    }

    open fun onBindViewHolder(binding: ItemBottomSheetListBinding, position: Int, t: T) {
        if (isNightMode()) {
            binding.ivLogo.imageTintList = ColorStateList.valueOf(Color.WHITE)
        }
    }

    private class Adapter<T>(val data: List<T>, val fragment: BottomSheetListFragment<T>) :
        RecyclerView.Adapter<Holder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
            val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_bottom_sheet_list, parent, false)
            return Holder(itemView)
        }

        override fun onBindViewHolder(holder: Holder, position: Int) {
            fragment.onBindViewHolder(holder.binding, position, data[position])
        }

        override fun getItemCount(): Int {
            return data.size
        }
    }

    private class Holder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = ItemBottomSheetListBinding.bind(itemView)
    }
}