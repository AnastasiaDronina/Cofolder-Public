package com.dronina.cofolder.ui.notes

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
import com.dronina.cofolder.data.model.entities.NoteFile
import com.dronina.cofolder.utils.extensions.*

class NotesRvAdapter(
    private var listener: OnItemClickListener,
    private val asGrid: Boolean
) : ListAdapter<NoteFile, NotesRvAdapter.NoteViewHolder>(NoteDiffCallback()), Filterable {
    private var list = ArrayList<NoteFile>()
    private var fullList = ArrayList<NoteFile>()


    interface OnItemClickListener {
        fun onItemClick(touchPosition: Pair<Int, Int>, note: NoteFile?)
        fun onItemMenuClick(note: NoteFile?): Boolean
    }

    override fun submitList(list: MutableList<NoteFile>?) {
        super.submitList(list)
        list?.let {
            fullList.clear()
            fullList.addAll(list)
            this.list.clear()
            this.list.addAll(list)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = if (asGrid) {
            LayoutInflater.from(parent.context).inflate(R.layout.note_rv_item_grid, parent, false)
        } else {
            LayoutInflater.from(parent.context).inflate(R.layout.note_rv_item, parent, false)
        }
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class NoteDiffCallback : DiffUtil.ItemCallback<NoteFile>() {
        override fun areItemsTheSame(oldItem: NoteFile, newItem: NoteFile): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: NoteFile, newItem: NoteFile): Boolean =
            oldItem.id == newItem.id
    }

    inner class NoteViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener,
        View.OnTouchListener {
        private var note: NoteFile? = null
        private var tvNoteName: TextView? = null
        private var tvNoteText: TextView? = null
        private var tvNoteDate: TextView? = null
        private var btnMenu: ImageButton? = null
        private var x: Int = 0
        private var y: Int = 0

        init {
            tvNoteName = view.findViewById(R.id.tv_note_name)
            tvNoteText = view.findViewById(R.id.tv_note_text)
            tvNoteDate = view.findViewById(R.id.tv_date)
            btnMenu = view.findViewById(R.id.btn_menu)
        }

        fun bind(note: NoteFile) {
            this.note = note
            itemView.paint(note.color)
            if (note.name.isNotEmpty()) {
                tvNoteName?.text = note.name.shortAsName(asGrid)
            } else {
                tvNoteName?.text = note.text.createNameFromText(asGrid)
            }
            tvNoteText?.text = note.text.short()
            tvNoteDate?.text = note.dateOfLastEdit.formatAsDate()

            itemView.setOnClickListener(this)
            itemView.setOnTouchListener(this)
            btnMenu?.setOnClickListener(this)
        }

        override fun onClick(view: View?) {
            when (view) {
                itemView -> listener.onItemClick(Pair(x, y), note)
                btnMenu -> listener.onItemMenuClick(note)
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

    private val filterNotes: Filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence): FilterResults {
            val results = FilterResults()
            results.values = if (constraint.isEmpty()) {
                fullList
            } else {
                fullList.filter { note ->
                    note.toString().toLowerCase()
                        .contains(constraint.toString().toLowerCase().trim { it <= ' ' })
                } as MutableList<NoteFile>
            }
            return results
        }

        override fun publishResults(constraint: CharSequence, results: FilterResults) {
            list.clear()
            list.addAll(results.values as List<NoteFile>)
            notifyDataSetChanged()
        }
    }

    override fun getFilter(): Filter {
        return filterNotes
    }
}