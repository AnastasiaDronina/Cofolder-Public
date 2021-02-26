package com.dronina.cofolder.ui.addcontributors

import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.dronina.cofolder.R
import com.dronina.cofolder.data.model.entities.User
import com.dronina.cofolder.databinding.FragmentAddContributorsBinding
import com.dronina.cofolder.ui.base.BaseFragment
import com.dronina.cofolder.ui.base.BaseViewModelFactory
import com.dronina.cofolder.ui.main.MainViewModel
import com.dronina.cofolder.ui.share.ShareViewModel
import com.dronina.cofolder.utils.extensions.*
import com.dronina.cofolder.utils.other.VERTICAL

class AddContributorsFragment : BaseFragment(),
    ContributorsToAddRvAdapter.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener {
    private lateinit var binding: FragmentAddContributorsBinding
    private lateinit var viewModel: AddContributorsViewModel
    private lateinit var mainViewModel: MainViewModel
    private lateinit var shareViewModel: ShareViewModel

    private var recyclerViewAdapter: ContributorsToAddRvAdapter? = null

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        inflater.inflate(R.menu.save, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_save -> {
                viewModel.save()
                super.onOptionsItemSelected(item)
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?): View {
        binding = DataBindingUtil
            .inflate(inflater, R.layout.fragment_add_contributors, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun initViewModels() {
        viewModel = ViewModelProviders
            .of(requireActivity(), BaseViewModelFactory(requireContext()))
            .get(AddContributorsViewModel::class.java)
        mainViewModel =
            ViewModelProviders.of(requireActivity(), BaseViewModelFactory(requireContext()))
                .get(MainViewModel::class.java)
        shareViewModel = ViewModelProviders
            .of(requireActivity(), BaseViewModelFactory(requireContext()))
            .get(ShareViewModel::class.java)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        viewModel.view = this

        if (viewModel.sync(shareViewModel, mainViewModel)) {
            recyclerViewAdapter = null
        }
    }

    override fun addObservers() {
        viewModel.currentContributors.observe(viewLifecycleOwner, {
            refresh()
            viewModel.currentContributors.value?.let { currentContributors ->
                if ((currentContributors.filter { it.second }).isEmpty()) {
                    binding.btnDone?.visibility = View.GONE
                } else {
                    if (binding.btnDone?.visibility == View.GONE) {
                        binding.btnDone?.fadeInUp()
                    }
                    binding.btnDone?.visibility = View.VISIBLE
                }
            }
        })
    }

    override fun addListeners() {
        binding.btnAddFriends.setOnClickListener {
            findNavController().navigate(R.id.action_addContributors_to_addFriends)
        }
        binding.btnDone?.setOnClickListener {
            viewModel.save()
        }
        binding.swipeToRefresh.setOnRefreshListener(this)
    }

    override fun refresh() {
        viewModel.currentContributors.value?.let { currentContributors ->
            hideProgress()
            binding.btnAddFriends.setVisibility(currentContributors.isEmpty())
            recyclerViewAdapter?.let {
                recyclerViewAdapter?.update(currentContributors)
            } ?: run {
                attachNewAdapter(currentContributors)
            }
            binding.rvFriends.animateViewAppearance(animate = viewModel.openRvWithAnimation)
            viewModel.openRvWithAnimation = false
        }
    }

    override fun showProgress() {
        binding.llProgressBar.visibility = View.VISIBLE
        binding.btnDone?.visibility = View.GONE
    }

    override fun hideProgress() {
        binding.llProgressBar.visibility = View.GONE
    }

    override fun onItemClick(contributor: Pair<User, Boolean>?) {
        viewModel.onClick(contributor)
    }

    override fun onRefresh() {
        viewModel.openRvWithAnimation = true
        viewModel.onViewCreated(mainViewModel.currentFriends.value)
        refresh()
        binding.swipeToRefresh.isRefreshing = false
    }

    private fun attachNewAdapter(currentContributors: List<Pair<User, Boolean>>) {
        recyclerViewAdapter = ContributorsToAddRvAdapter(currentContributors, this)
        binding.rvFriends.setHasFixedSize(true)
        binding.rvFriends.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFriends.setItemDecorations(VERTICAL)
        binding.rvFriends.adapter = recyclerViewAdapter
    }
}
