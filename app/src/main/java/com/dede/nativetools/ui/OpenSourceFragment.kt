package com.dede.nativetools.ui

import android.os.Bundle
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
import org.json.JSONArray

/**
 * 开源相关
 */
class OpenSourceFragment : Fragment(R.layout.fragment_open_source) {

    private val binding by viewBinding(FragmentOpenSourceBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val arrayList = loadOpenSource()
        binding.recyclerView.adapter = object : RecyclerView.Adapter<VH>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
                val itemView =
                    layoutInflater.inflate(R.layout.item_open_source, parent, false)
                return VH(itemView)
            }

            override fun onBindViewHolder(holder: VH, position: Int) {
                val openSource = arrayList[position]
                holder.bind(openSource)
            }

            override fun getItemCount(): Int {
                return arrayList.size
            }
        }
    }

    private class VH(view: View) : RecyclerView.ViewHolder(view) {
        val binding = ItemOpenSourceBinding.bind(view)

        fun bind(openSource: OpenSource) {
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
    )

    private fun loadOpenSource(): List<OpenSource> {
        val readBytes = resources.assets.open("open_source.json").buffered().use {
            it.readBytes()
        }
        val arrayList = ArrayList<OpenSource>()
        val jsonArray = JSONArray(String(readBytes))
        for (i in (0 until jsonArray.length())) {
            val jsonObject = jsonArray.getJSONObject(i)
            val name = jsonObject.getString("name")
            val desc = jsonObject.getString("desc")
            val url = jsonObject.optString("url")
            val author = jsonObject.optString("author")
            val openSource = OpenSource(name, desc, author, url)
            arrayList.add(openSource)
        }
        return arrayList
    }
}
