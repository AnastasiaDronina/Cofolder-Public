package com.dronina.cofolder.ui.friends

import android.view.*
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dronina.cofolder.R
import com.dronina.cofolder.data.model.entities.User
import com.dronina.cofolder.databinding.FragmentFriendsBinding
import com.dronina.cofolder.ui.base.BaseFragment
import com.dronina.cofolder.ui.base.BaseViewModelFactory
import com.dronina.cofolder.ui.main.MainViewModel
import com.dronina.cofolder.utils.other.USER_BUNDLE
import com.dronina.cofolder.utils.extensions.setItemDecorations
import com.dronina.cofolder.utils.extensions.setSearchView
import com.dronina.cofolder.utils.extensions.setVisibility
import com.dronina.cofolder.utils.other.VERTICAL

class FriendsFragment : BaseFragment(), FriendsRvAdapter.OnItemClickListener {
    private lateinit var binding: FragmentFriendsBinding
    private lateinit var viewModel: FriendsViewModel
    private lateinit var mainViewModel: MainViewModel
    private var recyclerViewAdapter: FriendsRvAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_friends, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun initViewModels() {
        viewModel = ViewModelProviders
            .of(this, BaseViewModelFactory(requireContext()))
            .get(FriendsViewModel::class.java)
        mainViewModel = ViewModelProviders
            .of(requireActivity(), BaseViewModelFactory(requireContext()))
            .get(MainViewModel::class.java)
        viewModel.view = this
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
    }

    override fun addObservers() {
        mainViewModel.currentFriends.observe(viewLifecycleOwner, {
            refresh()
        })
    }

    override fun addListeners() {
        binding.btnAddFriends.setOnClickListener {
            (requireActivity() as AppCompatActivity).supportActionBar?.title =
                getString(R.string.add_friends)
            findNavController().navigate(R.id.action_friends_to_addFriends)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        inflater.inflate(R.menu.friends_menu, menu)
        setSearchView(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_requests -> findNavController().navigate(R.id.action_friends_to_requests)
            R.id.action_add_friends -> {
                (requireActivity() as AppCompatActivity).supportActionBar?.title =
                    getString(R.string.add_friends)
                findNavController().navigate(R.id.action_friends_to_addFriends)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setSearchView(menu: Menu) {
        requireActivity().setSearchView(
            menu,
            R.id.action_search_friend,
            object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String): Boolean {
                    recyclerViewAdapter?.filter?.filter(newText)
                    return false
                }
            })
    }

    override fun refresh() {
        mainViewModel.currentFriends.value?.let { friends ->
            recyclerViewAdapter = FriendsRvAdapter(friends as ArrayList<User>, this)
            binding.rvFriends.setHasFixedSize(true)
            binding.rvFriends.layoutManager = LinearLayoutManager(requireContext())
            binding.rvFriends.setItemDecorations(VERTICAL)
            binding.rvFriends.adapter = recyclerViewAdapter
            binding.btnAddFriends.setVisibility(friends.isEmpty())
        }
    }

    override fun onItemClick(user: User?, extras: FragmentNavigator.Extras?) {
        (requireActivity() as AppCompatActivity).supportActionBar?.title = user?.toString()
        val bundle = bundleOf(USER_BUNDLE to user)
        extras?.let {
            findNavController().navigate(R.id.action_friends_to_publicProfile, bundle, null, extras)
        } ?: run {
            findNavController().navigate(R.id.action_friends_to_publicProfile, bundle)
        }
    }
}
