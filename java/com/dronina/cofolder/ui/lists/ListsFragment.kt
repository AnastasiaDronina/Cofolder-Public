package com.dronina.cofolder.ui.lists

import android.os.Bundle
import android.view.*
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.dronina.cofolder.R
import com.dronina.cofolder.data.model.entities.ListFile
import com.dronina.cofolder.databinding.FragmentListsBinding
import com.dronina.cofolder.ui.base.BaseFragment
import com.dronina.cofolder.ui.base.BaseViewModelFactory
import com.dronina.cofolder.ui.main.MainViewModel
import com.dronina.cofolder.utils.extensions.*
import com.dronina.cofolder.utils.other.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ListsFragment : BaseFragment(), ListsRvAdapter.OnItemClickListener,
    SwipeRefreshLayout.OnRefreshListener {
    private lateinit var binding: FragmentListsBinding
    private lateinit var viewModel: ListsViewModel
    private lateinit var mainViewModel: MainViewModel
    private var recyclerViewAdapter: ListsRvAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_lists, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated() {
        showProgress()
    }

    override fun initViewModels() {
        viewModel = ViewModelProviders
            .of(requireActivity(), BaseViewModelFactory(requireContext()))
            .get(ListsViewModel::class.java)
        mainViewModel = ViewModelProviders
            .of(requireActivity(), BaseViewModelFactory(requireContext()))
            .get(MainViewModel::class.java)
        viewModel.view = this
        recyclerViewAdapter = null
        mainViewModel.onCreate()
    }

    override fun addListeners() {
        binding.btnAddList.setOnClickListener {
            (requireActivity() as AppCompatActivity).supportActionBar?.title =
                getString(R.string.create_list)
            viewModel.addListClicked()
        }
        binding.swipeToRefresh.setOnRefreshListener(this)
    }

    override fun addObservers() {
        mainViewModel.currentLists.observe(viewLifecycleOwner, { lists ->
            viewModel.allLists.value = lists as ArrayList<ListFile>
            viewModel.sort()
        })
        viewModel.listsSorted.observe(viewLifecycleOwner, {
            refresh()
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        inflater.inflate(R.menu.lists_menu, menu)
        setSearchView(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_add_list -> {
                (requireActivity() as AppCompatActivity).supportActionBar?.title =
                    getString(R.string.create_list)
                viewModel.addListClicked()
            }
            R.id.sub_menu -> {
                if (!viewModel.canClick) return false
                viewModel.canClick = false
                showMenuPopup(
                    viewModel.showAsGird(),
                    { viewModel.setShowAsGrid() },
                    { navigateSortPage() }
                )
                GlobalScope.launch {
                    delay(500)
                    viewModel.canClick = true
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun refresh() {
        viewModel.listsSorted.value?.let { listsSorted ->
            hideProgress()
            binding.btnAddList.setVisibility(listsSorted.size <= 0)
            if (recyclerViewAdapter == null) {
                setRecyclerView(listsSorted)
            } else {
                recyclerViewAdapter?.submitList(listsSorted)
            }
        }
    }

    fun setRecyclerView(listsSorted: ArrayList<ListFile>) {
        binding.rvLists.setHasFixedSize(true)
        if (viewModel.showAsGird()) {
            binding.rvLists.layoutManager =
                StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
            binding.rvLists.setItemDecorations(GRID)
        } else {
            binding.rvLists.layoutManager = LinearLayoutManager(requireContext())
            binding.rvLists.setItemDecorations(VERTICAL)
        }
        recyclerViewAdapter = ListsRvAdapter(this, viewModel.showAsGird())
        recyclerViewAdapter?.submitList(listsSorted)
        binding.rvLists.adapter = recyclerViewAdapter

        if (viewModel.scrollPosition != 0) {
            binding.rvLists.scrollToPosition(viewModel.scrollPosition)
        }
        binding.rvLists.animateViewAppearance(viewModel.openRvWithAnimation)
        viewModel.openRvWithAnimation = false

        binding.rvLists.addSwipes(
            context = requireContext(),
            enabled = !viewModel.showAsGird(),
            onMove = { holder, target ->
                viewModel.onItemRangeChanged(holder.adapterPosition, target.adapterPosition)
                binding.rvLists.adapter
                    ?.notifyItemMoved(holder.adapterPosition, target.adapterPosition)
            },
            onSwiped = { holder ->
                viewModel.onSwiped(holder.adapterPosition)
            },
            onChildDraw = { canvas, holder, dX ->
                requireContext().onChildDraw(
                    isCreator = viewModel.isCreator(holder.adapterPosition),
                    canvas = canvas,
                    viewHolder = holder,
                    dimentionX = dX
                )
            }
        )
    }

    fun removeFromList(id: String) {
        mainViewModel.removeItem(LIST, id)
    }

    fun navigateEditPage(arguments: Bundle) {
        findNavController().navigate(R.id.action_lists_to_editList, arguments)
    }

    fun showBottomSheet(bundle: Bundle) {
        findNavController().navigate(R.id.action_lists_to_bottomSheet, bundle)
    }

    fun showConfirmationDialog(message: String) {
        viewModel.listsSorted.value?.let { listsSorted ->
            requireContext().showConfirmationDialog(
                message = message,
                positiveOnClick = { viewModel.confirmed() },
                negativeOnClick = { setRecyclerView(listsSorted) })
        }
    }

    override fun onItemClick(touchPosition: Pair<Int, Int>, list: ListFile?) {
        (requireActivity() as AppCompatActivity).supportActionBar?.title = list?.name
        viewModel.onItemClick(touchPosition, list)
    }

    override fun onItemMenuClick(list: ListFile?): Boolean {
        return viewModel.onItemMenuClick(list)
    }

    override fun onRefresh() {
        viewModel.scrollPosition = 0
        viewModel.openRvWithAnimation = true
        mainViewModel.refreshLists()
        viewModel.listsSorted.value?.let { listsSorted -> setRecyclerView(listsSorted) }
        binding.swipeToRefresh.isRefreshing = false
    }

    override fun onStop() {
        super.onStop()
        binding.rvLists.layoutManager?.let { layoutManager ->
            if (viewModel.showAsGird()) {
                viewModel.scrollPosition =
                    (layoutManager as StaggeredGridLayoutManager).findFirstVisibleItemPositions(null)[0]
            } else {
                viewModel.scrollPosition =
                    (layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition()
            }
        }
    }

    override fun showProgress() {
        binding.progressBar.visibility = View.VISIBLE
    }

    override fun hideProgress() {
        binding.progressBar.visibility = View.GONE
    }

    private fun navigateSortPage() {
        findNavController().navigate(R.id.action_lists_to_sort, bundleOf(SORT_BUNDLE to LIST))
    }

    private fun setSearchView(menu: Menu) {
        activity?.setSearchView(
            menu,
            R.id.action_search_list,
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
}
