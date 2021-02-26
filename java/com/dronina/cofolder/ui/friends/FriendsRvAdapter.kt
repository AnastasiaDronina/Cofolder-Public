package com.dronina.cofolder.ui.friends

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.recyclerview.widget.RecyclerView
import com.dronina.cofolder.R
import com.dronina.cofolder.data.model.entities.User
import com.dronina.cofolder.utils.extensions.setPicture
import com.facebook.drawee.view.SimpleDraweeView

class FriendsRvAdapter(
    val friends: ArrayList<User>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<FriendsRvAdapter.FriendViewHolder>(), Filterable {
    private var list = ArrayList<User>()
    private var fullList = ArrayList<User>()

    init {
        list.addAll(friends)
        fullList.addAll(friends)
    }

    interface OnItemClickListener {
        fun onItemClick(user: User?, extras: FragmentNavigator.Extras?)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        return FriendViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.friend_rv_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class FriendViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        private var user: User? = null
        private var tvUserName: TextView? = null
        private var tvUserPhone: TextView? = null
        private var ivProfilePic: SimpleDraweeView? = null

        init {
            tvUserName = view.findViewById(R.id.tv_name)
            tvUserPhone = view.findViewById(R.id.tv_phone)
            ivProfilePic = view.findViewById(R.id.iv_photo)
        }

        fun bind(user: User) {
            this.user = user
            tvUserName?.text = user.toString()
            tvUserPhone?.text = user.phone
            ivProfilePic?.setPicture(user.photoUrl)
            itemView.setOnClickListener(this)
        }

        override fun onClick(view: View?) {
            ViewCompat.setTransitionName(
                ivProfilePic as View,
                itemView.context.getString(R.string.transition_image)
            )
            ViewCompat.setTransitionName(
                tvUserName as View,
                itemView.context.getString(R.string.transition_textview)
            )
            ViewCompat.setTransitionName(
                tvUserPhone as View,
                itemView.context.getString(R.string.transition_phone)
            )

            val extras =
                FragmentNavigatorExtras(
                    ivProfilePic as View to itemView.context.getString(R.string.transition_image),
                    tvUserName as View to itemView.context.getString(R.string.transition_textview),
                    tvUserPhone as View to itemView.context.getString(R.string.transition_phone)
                )
            listener.onItemClick(user, extras)
        }
    }


    private val filterUsers: Filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence): FilterResults {
            val filteredList: MutableList<User> = ArrayList()
            if (constraint == null || constraint.isEmpty()) {
                filteredList.addAll(fullList)
            } else {
                val filterPattern =
                    constraint.toString().toLowerCase().trim { it <= ' ' }
                for (item in fullList) {
                    if (item.toString().toLowerCase().contains(filterPattern)) {
                        filteredList.add(item)
                    }
                }
            }
            val results = FilterResults()
            results.values = filteredList
            return results
        }

        override fun publishResults(
            constraint: CharSequence,
            results: FilterResults
        ) {
            list.clear()
            list.addAll(results.values as List<User>)
            notifyDataSetChanged()
        }
    }

    override fun getFilter(): Filter {
        return filterUsers
    }
}