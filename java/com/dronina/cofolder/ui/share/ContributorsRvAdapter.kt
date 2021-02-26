package com.dronina.cofolder.ui.share

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.ViewCompat
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.recyclerview.widget.RecyclerView
import com.dronina.cofolder.R
import com.dronina.cofolder.data.model.other.Contributor
import com.dronina.cofolder.utils.extensions.setPicture
import com.facebook.drawee.view.SimpleDraweeView
import com.google.android.material.switchmaterial.SwitchMaterial

class ContributorsRvAdapter(
    val amIEditor: Boolean,
    val contributors: ArrayList<Contributor>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<ContributorsRvAdapter.ContributorsViewHolder>(), Filterable {
    private var list = ArrayList<Contributor>()
    private var fullList = ArrayList<Contributor>()

    init {
        list.addAll(contributors)
        fullList.addAll(contributors)
    }

    interface OnItemClickListener {
        fun onItemClick(contributor: Contributor?, extras: FragmentNavigator.Extras?)
        fun onItemRemoveClick(contributor: Contributor?)
        fun onItemCheckChange(contributor: Contributor?, checked: Boolean?)
    }

    fun update(contributors: ArrayList<Contributor>) {
        fullList.clear()
        fullList.addAll(contributors)
        list.clear()
        list.addAll(contributors)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContributorsViewHolder {
        return ContributorsViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.contributor_rv_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ContributorsViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class ContributorsViewHolder(view: View) : RecyclerView.ViewHolder(view),
        View.OnClickListener {
        private var contributor: Contributor? = null
        private var tvName: TextView? = null
        private var tvPhone: TextView? = null
        private var btnRemove: ImageButton? = null
        private var ivCrown: ImageView? = null
        private var ivPicture: SimpleDraweeView? = null
        private var switchCanEdit: SwitchMaterial? = null

        init {
            tvName = view.findViewById(R.id.tv_name)
            tvPhone = view.findViewById(R.id.tv_phone)
            btnRemove = view.findViewById(R.id.btn_remove)
            ivCrown = view.findViewById(R.id.iv_crown)
            ivPicture = view.findViewById(R.id.iv_photo)
            switchCanEdit = view.findViewById(R.id.switch_can_edit)
        }

        fun bind(contributor: Contributor) {
            this.contributor = contributor
            switchCanEdit?.isChecked = contributor.isEditor || contributor.isCreator
            if (contributor.isMe || contributor.isCreator) {
                btnRemove?.visibility = View.INVISIBLE
                switchCanEdit?.isEnabled = false
                if (contributor.isMe && !contributor.isCreator) {
                    // is you (not creator)
                    ivCrown?.visibility = View.GONE
                }
                if (contributor.isCreator && !contributor.isMe) {
                    // is creator
                    ivCrown?.visibility = View.VISIBLE
                }
                if (contributor.isMe && contributor.isCreator) {
                    // is you and creator
                    ivCrown?.visibility = View.VISIBLE
                }
            } else {
                // nothing (not you, not creator)
                ivCrown?.visibility = View.GONE
                switchCanEdit?.isEnabled = amIEditor
                if (amIEditor) btnRemove?.visibility = View.VISIBLE
                else btnRemove?.visibility = View.INVISIBLE
            }
            tvName?.text = contributor.toString()
            tvPhone?.text = contributor.phone
            ivPicture?.setPicture(contributor.photoUrl)
            itemView.setOnClickListener(this)
            btnRemove?.setOnClickListener(this)
            switchCanEdit?.setOnClickListener(this)
        }

        override fun onClick(view: View?) {
            when (view) {
                itemView -> {
                    ViewCompat.setTransitionName(
                        ivPicture as View,
                        itemView.context.getString(R.string.transition_image)
                    )
                    ViewCompat.setTransitionName(
                        tvName as View,
                        itemView.context.getString(R.string.transition_textview)
                    )
                    ViewCompat.setTransitionName(
                        tvPhone as View,
                        itemView.context.getString(R.string.transition_phone)
                    )

                    val extras =
                        FragmentNavigatorExtras(
                            ivPicture as View to itemView.context.getString(R.string.transition_image),
                            tvName as View to itemView.context.getString(R.string.transition_textview),
                            tvPhone as View to itemView.context.getString(R.string.transition_phone)
                        )
                    listener.onItemClick(contributor, extras)
                }
                btnRemove -> listener.onItemRemoveClick(contributor)
                switchCanEdit -> listener.onItemCheckChange(contributor, switchCanEdit?.isChecked)
            }
        }
    }


    private val filterUsers: Filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence): FilterResults {
            val filteredList: MutableList<Contributor> = ArrayList()
            if (constraint == null || constraint.isEmpty()) {
                filteredList.addAll(fullList)
            } else {
                val filterPattern = constraint.toString().toLowerCase().trim { it <= ' ' }
                fullList.forEach { item ->
                    if (item.user.toString().toLowerCase().contains(filterPattern)) {
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
            list.addAll(results.values as List<Contributor>)
            notifyDataSetChanged()
        }
    }

    override fun getFilter(): Filter {
        return filterUsers
    }
}