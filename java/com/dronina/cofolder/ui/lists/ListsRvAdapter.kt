package com.dronina.cofolder.ui.lists

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dronina.cofolder.R
import com.dronina.cofolder.data.model.entities.ListFile
import com.dronina.cofolder.utils.extensions.*

class ListsRvAdapter(
    private val listener: OnItemClickListener,
    private val asGrid: Boolean
) : ListAdapter<ListFile, ListsRvAdapter.ListViewHolder>(ListDiffCallback()), Filterable {
    private var list = ArrayList<ListFile>()
    private var fullList = ArrayList<ListFile>()

    interface OnItemClickListener {
        fun onItemClick(touchPosition: Pair<Int, Int>, list: ListFile?)
        fun onItemMenuClick(list: ListFile?): Boolean
    }

    override fun submitList(list: MutableList<ListFile>?) {
        super.submitList(list)
        list?.let {
            fullList.clear()
            fullList.addAll(list)
            this.list.clear()
            this.list.addAll(list)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val view = if (asGrid) {
            LayoutInflater.from(parent.context).inflate(R.layout.list_rv_item_grid, parent, false)
        } else LayoutInflater.from(parent.context).inflate(R.layout.list_rv_item, parent, false)
        return ListViewHolder(view)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int = list.size

    class ListDiffCallback : DiffUtil.ItemCallback<ListFile>() {
        override fun areItemsTheSame(oldItem: ListFile, newItem: ListFile): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: ListFile, newItem: ListFile): Boolean =
            oldItem.id == newItem.id
    }

    inner class ListViewHolder(view: View) :
        RecyclerView.ViewHolder(view), View.OnClickListener, View.OnTouchListener {
        private var list: ListFile? = null
        private var tvListName: TextView? = null
        private var tvListContents: TextView? = null
        private var tvDate: TextView? = null
        private var btnMenu: ImageButton? = null

        private var x: Int = 0
        private var y: Int = 0

        init {
            tvListName = view.findViewById(R.id.tv_list_name)
            tvListContents = view.findViewById(R.id.tv_list_contents)
            tvDate = view.findViewById(R.id.tv_date)
            btnMenu = view.findViewById(R.id.btn_menu)
        }

        fun bind(list: ListFile) {
            this.list = list
            itemView.paint(list.color)
            if (list.name.isNotEmpty()) {
                tvListName?.text = list.name.shortAsName(asGrid)
            } else {
                tvListName?.text = list.items.formatAsText().createNameFromText(asGrid)
            }
            tvListContents?.text = list.items.formatAsText().short()
            tvDate?.text = list.dateOfLastEdit.formatAsDate()

            itemView.setOnClickListener(this)
            itemView.setOnTouchListener(this)
            btnMenu?.setOnClickListener(this)
        }

        override fun onClick(view: View?) {
            when (view) {
                itemView -> listener.onItemClick(Pair(x, y), list)
                btnMenu -> listener.onItemMenuClick(list)
            }
        }

        override fun onTouch(view: View?, event: MotionEvent?): Boolean {
            ifLet(view, event) { (view, event) ->
                val originalPos = IntArray(2)
                (view as View).getLocationInWindow(originalPos)
                val xRoot = originalPos[0] - view.width / 2
                val yRoot = originalPos[1] - view.height / 2
                if (asGrid) {
                    x = xRoot + (event as MotionEvent).x.toInt()
                    y = yRoot + event.y.toInt()
                } else {
                    x = (event as MotionEvent).x.toInt()
                    y = yRoot + event.y.toInt()
                }
            }
            return false
        }
    }

    private val filterLists: Filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence): FilterResults {
            val results = FilterResults()
            results.values = if (constraint.isEmpty()) fullList
            else fullList.filter { list ->
                list.toString().toLowerCase()
                    .contains(constraint.toString().toLowerCase().trim { it <= ' ' })
            } as MutableList<ListFile>
            return results
        }

        override fun publishResults(
            constraint: CharSequence,
            results: FilterResults
        ) {
            list.clear()
            list.addAll(results.values as List<ListFile>)
            notifyDataSetChanged()
        }
    }

    override fun getFilter(): Filter {
        return filterLists
    }
}