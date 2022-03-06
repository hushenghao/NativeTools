package com.dede.nativetools.ui

import android.app.Dialog
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
import com.dede.nativetools.main.applyBottomBarsInsets
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

open class BottomSheetListFragment<T> :
    AbsBottomSheetListFragment<T, BottomSheetListFragment.Holder>() {

    override fun onCreateViewHolder(parent: ViewGroup): Holder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bottom_sheet_list, parent, false)
        return Holder(itemView)
    }

    override fun setData(list: List<T>) {
        super.setData(list)
        if (list.size < 4) {
            binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), list.size)
        }
    }

    override fun onBindViewHolder(holder: Holder, position: Int, t: T) {
        onBindViewHolder(holder.binding, position, t)
    }

    open fun onBindViewHolder(binding: ItemBottomSheetListBinding, position: Int, t: T) {
    }

    class Holder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = ItemBottomSheetListBinding.bind(itemView)
    }
}

abstract class AbsBottomSheetListFragment<T, H : RecyclerView.ViewHolder> :
    BottomSheetDialogFragment() {

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
            @Suppress("VisibleForTests", "RestrictedApi")
            behavior.disableShapeAnimations()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        applyBottomBarsInsets(binding.recyclerView)
    }

    open fun setData(list: List<T>) {
        binding.recyclerView.adapter = Adapter(list, this)
    }

    abstract fun onCreateViewHolder(parent: ViewGroup): H

    abstract fun onBindViewHolder(holder: H, position: Int, t: T)

    private class Adapter<T, H : RecyclerView.ViewHolder>(
        val data: List<T>,
        val fragment: AbsBottomSheetListFragment<T, H>
    ) : RecyclerView.Adapter<H>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): H {
            return fragment.onCreateViewHolder(parent)
        }

        override fun onBindViewHolder(holder: H, position: Int) {
            fragment.onBindViewHolder(holder, position, data[position])
        }

        override fun getItemCount(): Int {
            return data.size
        }
    }

}