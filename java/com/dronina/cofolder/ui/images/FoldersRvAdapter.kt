package com.dronina.cofolder.ui.images

import android.view.LayoutInflater
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
import com.dronina.cofolder.data.model.entities.FolderFile
import com.dronina.cofolder.utils.extensions.formatAsDate
import com.dronina.cofolder.utils.extensions.paintFolder

class FoldersRvAdapter(
    private val listener: OnItemSelectedListener,
    private val asGrid: Boolean
) : ListAdapter<FolderFile, FoldersRvAdapter.FolderViewHolder>(FolderDiffCallback()), Filterable {
    private var list = ArrayList<FolderFile>()
    private var fullList = ArrayList<FolderFile>()

    interface OnItemSelectedListener {
        fun onShortClick(folder: FolderFile?)
        fun onMenuClick(folder: FolderFile?): Boolean
    }

    override fun submitList(list: MutableList<FolderFile>?) {
        super.submitList(list)
        list?.let {
            fullList.clear()
            fullList.addAll(list)
            this.list.clear()
            this.list.addAll(list)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
        val view = if (asGrid) {
            LayoutInflater.from(parent.context).inflate(R.layout.folder_rv_item_grid, parent, false)
        } else {
            LayoutInflater.from(parent.context).inflate(R.layout.folder_rv_item, parent, false)
        }
        return FolderViewHolder(view)
    }

    override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class FolderDiffCallback : DiffUtil.ItemCallback<FolderFile>() {
        override fun areItemsTheSame(oldItem: FolderFile, newItem: FolderFile): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: FolderFile, newItem: FolderFile): Boolean =
            oldItem.id == newItem.id
    }

    inner class FolderViewHolder(view: View) :
        RecyclerView.ViewHolder(view), View.OnClickListener {
        private var folder: FolderFile? = null
        private var tvFolderName: TextView? = null
        private var tvFolderDate: TextView? = null
        private var ivFolder: com.google.android.material.imageview.ShapeableImageView? = null
        private var btnMenu: ImageButton? = null

        init {
            tvFolderName = view.findViewById(R.id.tv_folder_name)
            tvFolderDate = view.findViewById(R.id.tv_date)
            ivFolder = view.findViewById(R.id.iv_folder)
            btnMenu = view.findViewById(R.id.btn_menu)
        }

        fun bind(folder: FolderFile) {
            this.folder = folder

            ivFolder?.paintFolder(folder.color)
            tvFolderName?.text = folder.name
            tvFolderDate?.text = folder.dateOfLastEdit.formatAsDate()
            itemView.setOnClickListener(this)
            btnMenu?.setOnClickListener(this)
        }

        override fun onClick(view: View?) {
            when (view) {
                itemView -> listener.onShortClick(folder)
                btnMenu -> listener.onMenuClick(folder)
            }
        }
    }

    private val filterFolders: Filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence): FilterResults {
            val results = FilterResults()
            results.values = if (constraint.isEmpty()) fullList
            else fullList.filter {
                it.toString().toLowerCase()
                    .contains(constraint.toString().toLowerCase().trim { it <= ' ' })
            } as MutableList<FolderFile>
            return results
        }

        override fun publishResults(constraint: CharSequence, results: FilterResults) {
            list.clear()
            list.addAll(results.values as List<FolderFile>)
            notifyDataSetChanged()
        }
    }

    override fun getFilter(): Filter {
        return filterFolders
    }
}