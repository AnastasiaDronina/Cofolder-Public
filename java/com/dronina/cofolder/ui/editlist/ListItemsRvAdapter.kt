package com.dronina.cofolder.ui.editlist

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.ImageButton
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dronina.cofolder.R
import com.dronina.cofolder.data.model.other.ListItem
import com.dronina.cofolder.utils.extensions.ifLet
import com.dronina.cofolder.utils.extensions.setVisibility
import com.dronina.cofolder.utils.extensions.showListItemMenu
import com.dronina.cofolder.utils.other.ALPHABETICALLY
import com.dronina.cofolder.utils.other.BY_DEFAULT
import com.dronina.cofolder.utils.other.CHECKED_FIRST
import com.dronina.cofolder.utils.other.UNCHECKED_FIRST

class ListItemsRvAdapter(
    val amIEditor: Boolean,
    val inflater: LayoutInflater,
    val list: ArrayList<ListItem>,
    val listener: OnItemClickListener,
    var sorting: Int?
) : ListAdapter<ListItem, ListItemsRvAdapter.ListItemsViewHolder>(ItemDiffCallback()) {
    var sortedList = ArrayList<ListItem>()

    interface OnItemClickListener {
        fun onCheckedChanged(listItem: ListItem, checked: Boolean)
        fun onEditItemClicked(listItem: ListItem)
        fun onRemoveItemClicked(listItem: ListItem)
    }

    class ItemDiffCallback : DiffUtil.ItemCallback<ListItem>() {
        override fun areItemsTheSame(oldItem: ListItem, newItem: ListItem): Boolean =
            oldItem == newItem

        override fun areContentsTheSame(oldItem: ListItem, newItem: ListItem): Boolean =
            oldItem == newItem
    }

    fun sort() {
        sorting?.let {
            val newList = ArrayList<ListItem>()
            when (sorting) {
                BY_DEFAULT -> updateList(list)
                CHECKED_FIRST -> {
                    newList.addAll(list)
                    newList.sortByDescending { it.checked }
                    updateList(newList)
                }
                UNCHECKED_FIRST -> {
                    newList.addAll(list)
                    newList.sortBy { it.checked }
                    updateList(newList)
                }
                ALPHABETICALLY -> {
                    newList.addAll(list)
                    newList.sortBy { it.text.toLowerCase() }
                    updateList(newList)
                }
            }
            notifyDataSetChanged()
        }
    }

    fun update(items: ArrayList<ListItem>) {
        list.clear()
        list.addAll(items)
        sortedList.clear()
        sort()
        notifyDataSetChanged()
    }

    fun getSortedItemsInCorrectRange(): ArrayList<ListItem> {
        return sortedList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListItemsViewHolder {
        return ListItemsViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.list_item_rv_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ListItemsViewHolder, position: Int) {
        holder.bind(sortedList[position])
    }

    override fun getItemCount(): Int {
        return sortedList.size
    }

    private fun updateList(newList: ArrayList<ListItem>) {
        sortedList.clear()
        sortedList.addAll(newList)
        submitList(sortedList)
    }

    inner class ListItemsViewHolder(view: View) : RecyclerView.ViewHolder(view),
        CompoundButton.OnCheckedChangeListener, View.OnClickListener {
        private var listItem: ListItem? = null
        private var cbListItem: CheckBox? = null
        private var btnMenu: ImageButton? = null

        init {
            cbListItem = view.findViewById(R.id.list_item_check_box)
            btnMenu = view.findViewById(R.id.btn_menu)
        }

        fun bind(listItem: ListItem) {
            this.listItem = listItem

            cbListItem?.text = listItem.text
            cbListItem?.isChecked = listItem.checked

            cbListItem?.let { checkBox ->
                if (listItem.checked) {
                    cbListItem?.paintFlags = checkBox.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                } else {
                    cbListItem?.paintFlags =
                        checkBox.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                }
            }
            btnMenu?.setVisibility(amIEditor)
            if (!amIEditor) cbListItem?.isEnabled = false
            else {
                cbListItem?.setOnCheckedChangeListener(this)
                btnMenu?.setOnClickListener(this)
            }
        }

        override fun onCheckedChanged(compoundButton: CompoundButton?, checked: Boolean) {
            listItem?.let { listener.onCheckedChanged(it, checked) }
            cbListItem?.let { checkBox ->
                if (checked) {
                    cbListItem?.paintFlags = checkBox.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                } else {
                    cbListItem?.paintFlags =
                        checkBox.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                }
            }
        }

        override fun onClick(view: View?) {
            ifLet(view, listItem) { (view, listItem) ->
                inflater.showListItemMenu(
                    view as View,
                    { listener.onEditItemClicked(listItem as ListItem) },
                    { listener.onRemoveItemClicked(listItem as ListItem) })
            }
        }
    }
}