package com.dronina.cofolder.ui.share

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.dronina.cofolder.R
import com.dronina.cofolder.data.model.other.Contributor
import com.dronina.cofolder.data.model.entities.User
import com.dronina.cofolder.databinding.FragmentShareBinding
import com.dronina.cofolder.ui.base.BaseFragment
import com.dronina.cofolder.ui.base.BaseViewModelFactory
import com.dronina.cofolder.ui.insidefolder.InsideFolderViewModel
import com.dronina.cofolder.ui.main.MainViewModel
import com.dronina.cofolder.utils.extensions.animateViewAppearance
import com.dronina.cofolder.utils.other.USER_BUNDLE
import com.dronina.cofolder.utils.extensions.setItemDecorations
import com.dronina.cofolder.utils.extensions.setVisibility
import com.dronina.cofolder.utils.other.VERTICAL

class ShareFragment : BaseFragment(), ContributorsRvAdapter.OnItemClickListener,
    SwipeRefreshLayout.OnRefreshListener {
    private lateinit var binding: FragmentShareBinding
    private lateinit var viewModel: ShareViewModel
    private lateinit var mainViewModel: MainViewModel
    private lateinit var insideFolderViewModel: InsideFolderViewModel
    private var recyclerViewAdapter: ContributorsRvAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_share, container, false)
        return binding.root
    }

    override fun initViewModels() {
        viewModel = ViewModelProviders
            .of(requireActivity(), BaseViewModelFactory(requireContext()))
            .get(ShareViewModel::class.java)
        mainViewModel = ViewModelProviders
            .of(requireActivity(), BaseViewModelFactory(requireContext()))
            .get(MainViewModel::class.java)
        insideFolderViewModel = ViewModelProviders
            .of(requireActivity(), BaseViewModelFactory(requireContext()))
            .get(InsideFolderViewModel::class.java)
        insideFolderViewModel.navigateWithoutTransition.value = true
        viewModel.view = this
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        recyclerViewAdapter = null
        arguments?.let {
            viewModel.onViewCreated(requireArguments())
            arguments = null
        }
    }

    override fun addListeners() {
        binding.swipeToRefresh.setOnRefreshListener(this)
        binding.clAddContributor.setOnClickListener {
            viewModel.addContributorOnClick()
        }
    }

    override fun addObservers() {
        viewModel.contributors.observe(viewLifecycleOwner, {
            refresh()
        })
        viewModel.amIEditor.observe(viewLifecycleOwner, { amIEditor ->
            binding.clAddContributor.setVisibility(amIEditor)
        })
    }

    override fun refresh() {
        viewModel.contributors.value?.let { contributors ->
            viewModel.amIEditor.value?.let { amIEditor ->
                if (recyclerViewAdapter == null) {
                    binding.rvContributors.setHasFixedSize(true)
                    recyclerViewAdapter = ContributorsRvAdapter(amIEditor, contributors, this)
                    binding.rvContributors.layoutManager = LinearLayoutManager(requireContext())
                    binding.rvContributors.setItemDecorations(VERTICAL)
                    binding.rvContributors.adapter = recyclerViewAdapter
                } else {
                    recyclerViewAdapter?.update(contributors)
                }
                binding.rvContributors.animateViewAppearance(viewModel.openRvWithAnimation)
                viewModel.openRvWithAnimation = false
            }
        }
    }

    override fun onItemClick(contributor: Contributor?, extras: FragmentNavigator.Extras?) {
        contributor?.let { contributos ->
            if (contributor.isMe) return
            val bundle = bundleOf(USER_BUNDLE to contributor.user)
            extras?.let {
                findNavController().navigate(
                    R.id.action_share_to_publicProfile,
                    bundle,
                    null,
                    extras
                )
            } ?: run {
                findNavController().navigate(R.id.action_share_to_publicProfile, bundle)
            }
        }
    }

    override fun onItemRemoveClick(contributor: Contributor?) {
        viewModel.removeItemFromRv(contributor)
        viewModel.removeContributor(contributor)
    }

    override fun onItemCheckChange(contributor: Contributor?, checked: Boolean?) {
        viewModel.onCheckedChange(contributor, checked)
    }

    override fun showProgress() {
        if (viewModel.contributors.value == null || viewModel.contributors.value?.size == 0) {
            binding.progressBar.visibility = View.VISIBLE
        }
    }

    override fun hideProgress() {
        binding.progressBar.visibility = View.GONE
    }

    override fun onRefresh() {
        viewModel.openRvWithAnimation = true
        viewModel.getContributorsFromRepo()
        refresh()
        binding.swipeToRefresh.isRefreshing = false
    }

    fun navigateAddContributorsPage(bundle: Bundle) {
        findNavController().navigate(R.id.action_share_to_addContributors, bundle)
    }

    fun getUsers(): List<User>? {
        return mainViewModel.currentUsers.value
    }

}
