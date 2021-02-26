package com.dronina.cofolder.ui.profile

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.dronina.cofolder.R
import com.dronina.cofolder.databinding.FragmentProfileBinding
import com.dronina.cofolder.ui.base.BaseFragment
import com.dronina.cofolder.ui.base.BaseViewModelFactory
import com.dronina.cofolder.ui.main.MainViewModel
import com.dronina.cofolder.utils.extensions.*
import com.dronina.cofolder.utils.other.IMAGE_DIR
import com.dronina.cofolder.utils.other.RC_PICK_IMAGES

class ProfileFragment : BaseFragment() {
    private lateinit var binding: FragmentProfileBinding
    private lateinit var viewModel: ProfileViewModel
    private lateinit var mainViewModel: MainViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile, container, false)
        return binding.root
    }

    override fun initViewModels() {
        viewModel = ViewModelProviders
            .of(this, BaseViewModelFactory(requireContext()))
            .get(ProfileViewModel::class.java)
        mainViewModel = ViewModelProviders
            .of(requireActivity(), BaseViewModelFactory(requireContext()))
            .get(MainViewModel::class.java)
        viewModel.view = this
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
    }

    override fun addListeners() {
        binding.ivProfilePhoto.setOnClickListener {
            activity?.showCustomDialog(
                title = getString(R.string.change_profile_photo),
                negativeText = getString(R.string.delete),
                positiveText = getString(R.string.change),
                positiveOnClick = { viewModel.profilePictureOnClick() },
                negativeOnClick = { viewModel.deleteProfilePicture() }
            )
        }
        binding.ivProfilePhoto.setOnLongClickListener {
            viewModel.profilePictureOnClick()
            true
        }
        binding.tvName.setOnClickListener {
            requireContext().showEditTextDialog(
                "",
                "",
                binding.tvName.text.toString(),
                { viewModel.saveName(it) }
            )
        }
        binding.tvSurname.setOnClickListener {
            requireContext().showEditTextDialog(
                "",
                "",
                binding.tvSurname.text.toString(),
                { viewModel.saveSurname(it) }
            )
        }
        binding.tvPhone.setOnClickListener {
            binding.tvPhone.copyText()
            requireActivity().toast(getString(R.string.phone_was_copied))
        }
        binding.tvPhone.setOnLongClickListener {
            binding.tvPhone.copyText()
            requireActivity().toast(getString(R.string.phone_was_copied))
            true
        }
        binding.btnFriends.setOnClickListener {
            findNavController().navigate(R.id.action_profile_to_friends)
        }
        binding.btnNotifications.setOnClickListener {
            findNavController().navigate(R.id.action_profile_to_requests)
        }
        binding.btnSettings.setOnClickListener {
            findNavController().navigate(R.id.action_profile_to_settings)
        }
        binding.btnAppInfo.setOnClickListener {
            findNavController().navigate(R.id.action_profile_to_about)
        }
        binding.btnSignOut.setOnClickListener {
            viewModel.logout()
        }
    }

    override fun addObservers() {
        mainViewModel.currentUser.observe(viewLifecycleOwner, Observer {
            viewModel.userUpdated(mainViewModel.currentUser.value)
        })

        viewModel.profileImage.observe(viewLifecycleOwner, Observer {
            setPicture(viewModel.profileImage.value)
        })
    }

    fun openFileChooser() {
        val intent = Intent()
        intent.type = IMAGE_DIR
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(
            Intent.createChooser(intent, getString(R.string.select_images)),
            RC_PICK_IMAGES
        )
    }

    override fun onActivityResult(rc: Int, result: Int, intent: Intent?) {
        if (rc == RC_PICK_IMAGES && result == Activity.RESULT_OK && null != android.R.attr.data) {
            requireContext().showConfirmationDialog(
                message = requireContext().getString(R.string.change_profile_photo),
                positiveOnClick = { viewModel.onActivityResult(rc, result, intent) }
            )
        }
        super.onActivityResult(rc, result, intent)
    }


    fun onLogoutSuccess() {
        requireActivity().navigateLaunchPage()
    }

    fun setPicture(uri: String?) {
        binding.ivProfilePhoto.setPicture(uri)
    }
}
