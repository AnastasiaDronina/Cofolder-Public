package com.dronina.cofolder.ui.addfriends

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.dronina.cofolder.R
import com.dronina.cofolder.data.model.entities.User
import com.dronina.cofolder.data.model.other.Contact
import com.dronina.cofolder.databinding.FragmentAddFriendsBinding
import com.dronina.cofolder.ui.base.BaseFragment
import com.dronina.cofolder.ui.base.BaseViewModelFactory
import com.dronina.cofolder.utils.extensions.*
import com.dronina.cofolder.utils.other.DARK_THEME
import com.dronina.cofolder.utils.other.RC_READ_CONTACTS
import com.dronina.cofolder.utils.other.VERTICAL
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.PermissionRequest


class AddFriendsFragment : BaseFragment(), EasyPermissions.PermissionCallbacks,
    ContactsRvAdapter.ContactClickListener {
    private lateinit var binding: FragmentAddFriendsBinding
    private lateinit var viewModel: AddFriendsViewModel

    private var recyclerViewAdapter: ContactsRvAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_add_friends, container, false)
        return binding.root
    }

    override fun initViewModels() {
        viewModel = ViewModelProviders
            .of(this, BaseViewModelFactory(requireContext()))
            .get(AddFriendsViewModel::class.java)
        viewModel.view = this
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
    }

    override fun addListeners() {
        binding.etPhone.doOnTextChanged { text, start, before, count ->
            binding.appBarLayout.setExpanded(true)
            recyclerViewAdapter?.filter?.filter(text)
        }
        binding.etContactName.doOnTextChanged { text, start, before, count ->
            binding.appBarLayout.setExpanded(true)
            recyclerViewAdapter?.filter?.filter(text)
        }
        binding.btnNext.setOnClickListener {
            requireActivity().showKeyboard(binding.etPhone)
            viewModel.processPhone()
        }
        binding.btnSearchContact.setOnClickListener {
            requireActivity().showKeyboard(binding.etContactName)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getContacts()
    }


    @AfterPermissionGranted(RC_READ_CONTACTS)
    private fun getContacts() {
        val permission = Manifest.permission.READ_CONTACTS
        if (EasyPermissions.hasPermissions(requireContext(), permission)) {
            viewModel.readContactsGranted(requireActivity().contentResolver)
            refresh()
        } else {
            val style = when (requireContext().whichTheme()) {
                DARK_THEME -> R.style.AlertDialogCustomDark
                else -> R.style.AlertDialogCustom
            }
            EasyPermissions.requestPermissions(
                PermissionRequest.Builder(this, RC_READ_CONTACTS, permission)
                    .setRationale(R.string.read_contacts_permission_explained)
                    .setPositiveButtonText(android.R.string.yes)
                    .setNegativeButtonText(android.R.string.no)
                    .setTheme(style)
                    .build()
            )
        }
    }

    override fun onRequestPermissionsResult(rc: Int, perms: Array<out String>, results: IntArray) {
        super.onRequestPermissionsResult(rc, perms, results)
        EasyPermissions.onRequestPermissionsResult(rc, perms, results, this)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        viewModel.readContactsGranted(requireActivity().contentResolver)
        refresh()
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {}

    override fun refresh() {
        viewModel.contacts.observe(viewLifecycleOwner, {
            viewModel.contacts.value?.let { contacts ->
                binding.rvContacts.setHasFixedSize(true)
                binding.rvContacts.layoutManager = LinearLayoutManager(requireContext())
                recyclerViewAdapter = ContactsRvAdapter(contacts, this)
                binding.rvContacts.setItemDecorations(VERTICAL)
                binding.rvContacts.adapter = recyclerViewAdapter
            }
        })
    }

    fun processPhoneOnStart() {
        binding.progressBar.visibility = View.VISIBLE
    }

    fun processPhoneOnSuccess(user: User) {
        navigatePublicProfile(user)
    }

    fun processPhoneOnFailure() {
        binding.progressBar.visibility = View.GONE
        requireActivity().toast(getString(R.string.please_enter_phone_number))
    }

    fun processPhoneOnError() {
        binding.progressBar.visibility = View.GONE
        requireActivity().toast(getString(R.string.no_user_with_this_phone))
    }

    fun enteredOwnPhone() {
        binding.progressBar.visibility = View.GONE
        requireActivity().toast(getString(R.string.entered_own_phone))
    }

    override fun showProgress() {
        binding.progressBar.visibility = View.VISIBLE
    }

    override fun hideProgress() {
        binding.progressBar.visibility = View.GONE
    }

    override fun onClick(contact: Contact) {
        binding.etPhone.setText(contact.phone)
        binding.etPhone.setSelection(contact.phone.length)
        viewModel.processPhone()
    }
}
