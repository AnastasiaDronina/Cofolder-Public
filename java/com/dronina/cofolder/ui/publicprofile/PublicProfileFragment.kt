package com.dronina.cofolder.ui.publicprofile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.transition.TransitionInflater
import com.dronina.cofolder.R
import com.dronina.cofolder.databinding.FragmentPublicProfileBinding
import com.dronina.cofolder.ui.base.BaseFragment
import com.dronina.cofolder.ui.base.BaseViewModelFactory
import com.dronina.cofolder.ui.main.MainViewModel
import com.dronina.cofolder.ui.share.ShareViewModel
import com.dronina.cofolder.utils.extensions.setPicture

class PublicProfileFragment : BaseFragment() {
    private lateinit var binding: FragmentPublicProfileBinding
    private lateinit var viewModel: PublicProfileViewModel
    private lateinit var mainViewModel: MainViewModel
    private lateinit var sharedViewModel: ShareViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_public_profile, container, false)
        val transition =
            TransitionInflater.from(context).inflateTransition(android.R.transition.move)
        sharedElementEnterTransition = transition
        sharedElementReturnTransition = transition
        return binding.root
    }

    override fun onViewCreated() {
        sharedElementEnterTransition =
            TransitionInflater.from(requireContext()).inflateTransition(android.R.transition.move)
    }

    override fun initViewModels() {
        viewModel = ViewModelProviders
            .of(this, BaseViewModelFactory(requireContext()))
            .get(PublicProfileViewModel::class.java)
        mainViewModel = ViewModelProviders
            .of(requireActivity(), BaseViewModelFactory(requireContext()))
            .get(MainViewModel::class.java)
        sharedViewModel = ViewModelProviders
            .of(requireActivity(), BaseViewModelFactory(requireContext()))
            .get(ShareViewModel::class.java)
        viewModel.view = this
        viewModel.contactView = requireView()
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        viewModel.onViewCreated(arguments)
    }

    override fun addListeners() {
        binding.btnSendRequest.setOnClickListener {
            viewModel.sendRequest()
        }
        binding.btnAccept.setOnClickListener {
            viewModel.acceptRequest()
        }
        binding.btnRemoveFriend.setOnClickListener {
            viewModel.removeFriend()
        }
        binding.btnCancelRequest.setOnClickListener {
            viewModel.cancelRequest()
        }
    }

    override fun addObservers() {
        mainViewModel.currentUser.observe(viewLifecycleOwner, { user ->
            viewModel.currentUser.value = user
        })
        viewModel.user.observe(viewLifecycleOwner, { user ->
            binding.ivProfilePic.setPicture(user.photoUrl)
        })
    }

    override fun showProgress() {
        binding.progressBar.visibility = View.VISIBLE
    }

    override fun hideProgress() {
        binding.progressBar.visibility = View.GONE
    }

}
