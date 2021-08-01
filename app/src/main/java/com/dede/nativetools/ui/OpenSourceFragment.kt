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
import com.dede.nativetools.util.assets
import com.dede.nativetools.util.browse
import com.dede.nativetools.util.isEmpty
import org.json.JSONArray
import org.json.JSONObject

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
        val desc: String,
        val author: String? = null,
        val url: String? = null
    ) {
        constructor(obj: JSONObject) : this(
            obj.getString("name"),
            obj.getString("desc"),
            obj.optString("author"),
            obj.optString("url")
        )
    }

    private fun loadOpenSource(): List<OpenSource> {
        val jsonStr = requireContext()
            .assets("open_source.json")
            .bufferedReader()
            .use { it.readText() }
        val list = ArrayList<OpenSource>()
        val jsonArray = JSONArray(jsonStr)
        for (i in (0 until jsonArray.length())) {
            val obj = jsonArray.getJSONObject(i)
            val openSource = OpenSource(obj)
            list.add(openSource)
        }
        return list
    }
}
