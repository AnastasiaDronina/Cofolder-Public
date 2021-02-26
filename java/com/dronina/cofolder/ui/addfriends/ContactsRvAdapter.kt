package com.dronina.cofolder.ui.addfriends

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dronina.cofolder.R
import com.dronina.cofolder.data.model.other.Contact

class ContactsRvAdapter(
    val list: List<Contact>,
    private val listener: ContactClickListener
) : RecyclerView.Adapter<ContactsRvAdapter.ContactViewHolder>(), Filterable {
    private var mList = ArrayList<Contact>()
    private var mFullList = ArrayList<Contact>()

    init {
        mList.addAll(list)
        mFullList.addAll(list)
    }

    interface ContactClickListener {
        fun onClick(contact: Contact)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.contact_rv_item, parent, false)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact: Contact = mList[position]
        holder.bind(contact)
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    inner class ContactViewHolder(view: View) :
        RecyclerView.ViewHolder(view), View.OnClickListener {
        private var contact: Contact? = null
        private var tvName: TextView? = null
        private var tvPhone: TextView? = null

        init {
            tvName = view.findViewById(R.id.tv_contact_name)
            tvPhone = view.findViewById(R.id.tv_contact_phone)
        }

        fun bind(contact: Contact) {
            this.contact = contact

            tvName?.text = contact.name
            tvPhone?.text = contact.phone

            itemView.setOnClickListener(this)
        }

        override fun onClick(view: View?) {
            contact?.let { contact ->
                when (view) {
                    itemView -> listener.onClick(contact)
                }
            }
        }
    }

    override fun getFilter(): Filter {
        return filterContacts
    }

    private val filterContacts: Filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence): FilterResults {
            val filteredList: MutableList<Contact> = ArrayList()
            if (constraint == null || constraint.isEmpty()) {
                filteredList.addAll(mFullList)
            } else {
                val filterPattern =
                    constraint.toString().toLowerCase().trim { it <= ' ' }
                for (item in mFullList) {
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
            mList.clear()
            mList.addAll(results.values as List<Contact>)
            notifyDataSetChanged()
        }
    }
}