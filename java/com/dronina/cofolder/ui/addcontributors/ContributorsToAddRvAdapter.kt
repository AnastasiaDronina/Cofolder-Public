package com.dronina.cofolder.ui.addcontributors

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dronina.cofolder.R
import com.dronina.cofolder.data.model.entities.User
import com.dronina.cofolder.utils.extensions.setPicture
import com.facebook.drawee.view.SimpleDraweeView


class ContributorsToAddRvAdapter(
    val list: List<Pair<User, Boolean>>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<ContributorsToAddRvAdapter.ContributorsToAddViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(contributor: Pair<User, Boolean>?)
    }

    fun update(newList: List<Pair<User, Boolean>>) {
        (list as ArrayList<Pair<User, Boolean>>).clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContributorsToAddViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.contributor_to_add_rv_item, parent, false)
        return ContributorsToAddViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContributorsToAddViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class ContributorsToAddViewHolder(view: View) :
        RecyclerView.ViewHolder(view), View.OnClickListener {
        private var contributor: Pair<User, Boolean>? = null
        private var tvName: TextView? = null
        private var tvPhone: TextView? = null
        private var checkBox: CheckBox? = null
        private var ivPicture: SimpleDraweeView? = null

        init {
            tvName = view.findViewById(R.id.tv_name)
            tvPhone = view.findViewById(R.id.tv_phone)
            checkBox = view.findViewById(R.id.check_box)
            ivPicture = view.findViewById(R.id.iv_photo)
        }

        fun bind(contributor: Pair<User, Boolean>) {
            this.contributor = contributor

            tvName?.text = contributor.first.toString()
            tvPhone?.text = contributor.first.phone
            checkBox?.isChecked = contributor.second
            ivPicture?.setPicture(contributor.first.photoUrl)

            itemView.setOnClickListener(this)
            checkBox?.setOnClickListener(this)
        }

        override fun onClick(view: View?) {
            listener.onItemClick(contributor)
            contributor?.second?.let {
                checkBox?.isChecked = !it
            }
        }
    }
}