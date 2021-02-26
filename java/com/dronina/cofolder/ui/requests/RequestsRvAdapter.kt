package com.dronina.cofolder.ui.requests

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dronina.cofolder.R
import com.dronina.cofolder.data.model.entities.Request
import com.dronina.cofolder.utils.extensions.setPicture
import com.facebook.drawee.view.SimpleDraweeView


interface OnItemClickListener {
    fun onItemClick(request: Request?)
    fun cancelOnClick(request: Request?, position: Int)
    fun acceptOnClick(request: Request?, position: Int)
}

class ToMeRvAdapter(
    val requests: ArrayList<Request>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<ToMeRvAdapter.ToMeViewHolder>() {

    fun update(updatedRequests: ArrayList<Request>) {
        requests.clear()
        requests.addAll(updatedRequests)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToMeViewHolder {
        return ToMeViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.request_to_me_rv_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ToMeViewHolder, position: Int) {
        holder.bind(requests[position], position)
    }

    override fun getItemCount(): Int {
        return requests.size
    }

    inner class ToMeViewHolder(view: View) : RecyclerView.ViewHolder(view),
        View.OnClickListener {
        private var request: Request? = null
        private var position: Int? = null
        private var tvUserName: TextView? = null
        private var ivProfilePic: SimpleDraweeView? = null
        private var btnAcceptRequest: Button? = null

        init {
            tvUserName = view.findViewById(R.id.tv_user_name)
            ivProfilePic = view.findViewById(R.id.iv_profile_pic)
            btnAcceptRequest = view.findViewById(R.id.btn_accept_request)
        }

        fun bind(request: Request, position: Int) {
            this.request = request

            this.position = position
            tvUserName?.text = request.sender?.toString()
            ivProfilePic?.setPicture(request.sender?.photoUrl)
            itemView.setOnClickListener(this)
            btnAcceptRequest?.setOnClickListener(this)
        }

        override fun onClick(view: View?) {
            position?.let { pos ->
                when (view) {
                    itemView -> listener.onItemClick(request)
                    btnAcceptRequest -> if (position != null) listener.acceptOnClick(request, pos)
                }
            }
        }
    }
}

class ByMeRvAdapter(
    val requests: ArrayList<Request>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<ByMeRvAdapter.ByMeViewHolder>() {

    fun update(updatedRequests: ArrayList<Request>) {
        requests.clear()
        requests.addAll(updatedRequests)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ByMeViewHolder {
        return ByMeViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.request_by_me_rv_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ByMeViewHolder, position: Int) {
        val request: Request = requests[position]
        holder.bind(request, position)
    }

    override fun getItemCount(): Int {
        return requests.size
    }

    inner class ByMeViewHolder(view: View) : RecyclerView.ViewHolder(view),
        View.OnClickListener {
        private var request: Request? = null
        private var position: Int? = null
        private var tvUserName: TextView? = null
        private var ivProfilePic: SimpleDraweeView? = null
        private var btnCancelRequest: Button? = null

        init {
            tvUserName = view.findViewById(R.id.tv_user_name)
            ivProfilePic = view.findViewById(R.id.iv_profile_pic)
            btnCancelRequest = view.findViewById(R.id.btn_cancel_request)
        }

        fun bind(request: Request, position: Int) {
            this.request = request
            this.position = position
            tvUserName?.text = request.receiver?.toString()
            ivProfilePic?.setPicture(request.receiver?.photoUrl)
            itemView.setOnClickListener(this)
            btnCancelRequest?.setOnClickListener(this)
        }

        override fun onClick(view: View?) {
            position?.let { pos ->
                when (view) {
                    itemView -> listener.onItemClick(request)
                    btnCancelRequest -> if (position != null) listener.cancelOnClick(request, pos)
                }
            }
        }
    }
}