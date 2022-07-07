package com.dede.nativetools.ui

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.dede.nativetools.R
import com.dede.nativetools.databinding.FragmentBottomSheetListBinding
import com.dede.nativetools.databinding.ItemBottomSheetListBinding
import com.dede.nativetools.main.WindowEdgeManager
import com.dede.nativetools.network.isLoading
import com.dede.nativetools.util.toast
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

    private lateinit var adapter: Adapter<T, H>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        applyBottomBarsInsets(binding.recyclerView)
        adapter = Adapter(this)
        binding.recyclerView.adapter = adapter
    }

    open fun setData(list: List<T>) {
        adapter.setData(list)
        binding.progressCircular.isVisible = false
    }

    open fun setData(result: Result<List<T>>) {
        if (result.isLoading) {
            binding.progressCircular.isVisible = true
            return
        }
        if (result.isFailure) {
            toast(R.string.toast_network_error)
            dismissAllowingStateLoss()
            return
        }
        this.setData(result.getOrThrow())
    }

    abstract fun onCreateViewHolder(parent: ViewGroup): H

    abstract fun onBindViewHolder(holder: H, position: Int, t: T)

    private class Adapter<T, H : RecyclerView.ViewHolder>(val fragment: AbsBottomSheetListFragment<T, H>) :
        RecyclerView.Adapter<H>() {

        private val data: MutableList<T> = ArrayList()

        fun setData(list: List<T>) {
            data.clear()
            notifyItemRangeRemoved(0, itemCount)
            data.addAll(list)
            notifyItemRangeInserted(0, itemCount)
        }

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