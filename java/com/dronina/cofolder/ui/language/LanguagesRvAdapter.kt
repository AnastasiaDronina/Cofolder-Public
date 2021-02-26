package com.dronina.cofolder.ui.language

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dronina.cofolder.R

class LanguagesRvAdapter(
    languages: List<String>,
    private val listener: OnItemSelectedListener
) : RecyclerView.Adapter<LanguagesRvAdapter.LanguagesViewHolder>(), Filterable {
    private var list = ArrayList<String>()
    private var fullList = ArrayList<String>()

    init {
        list.addAll(languages)
        fullList.addAll(languages)
    }

    interface OnItemSelectedListener {
        fun onShortClick(language: String?)
    }

    fun update(languages: ArrayList<String>) {
        fullList.clear()
        fullList.addAll(languages)
        list.clear()
        list.addAll(languages)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguagesViewHolder {
        return LanguagesViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.language_rv_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: LanguagesViewHolder, position: Int) {
        val language: String = list[position]
        holder.bind(language)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class LanguagesViewHolder(view: View) :
        RecyclerView.ViewHolder(view), View.OnClickListener {
        private var language: String? = null
        private var tvLanguage: TextView? = null

        init {
            tvLanguage = view.findViewById(R.id.tv_language)
        }

        fun bind(language: String) {
            this.language = language
            tvLanguage?.text = language
            itemView.setOnClickListener(this)
        }

        override fun onClick(view: View?) {
            listener.onShortClick(language)
        }
    }

    private val filterLanguages: Filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence): FilterResults {
            val filteredList: MutableList<String> = ArrayList()
            if (constraint == null || constraint.isEmpty()) {
                filteredList.addAll(fullList)
            } else {
                val filterPattern = constraint.toString().toLowerCase().trim { it <= ' ' }
                fullList.forEach { item ->
                    if (item.toLowerCase().trim().contains(filterPattern)) {
                        filteredList.add(item)
                    }
                }
            }
            val results = FilterResults()
            results.values = filteredList
            return results
        }

        override fun publishResults(constraint: CharSequence, results: FilterResults) {
            list.clear()
            list.addAll(results.values as List<String>)
            notifyDataSetChanged()
        }
    }

    override fun getFilter(): Filter {
        return filterLanguages
    }
}