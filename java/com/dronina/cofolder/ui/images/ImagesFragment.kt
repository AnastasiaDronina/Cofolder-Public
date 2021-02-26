package com.dronina.cofolder.ui.images

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
import com.dronina.cofolder.data.model.entities.FolderFile
import com.dronina.cofolder.databinding.FragmentImagesBinding
import com.dronina.cofolder.ui.base.BaseFragment
import com.dronina.cofolder.ui.base.BaseViewModelFactory
import com.dronina.cofolder.ui.insidefolder.InsideFolderViewModel
import com.dronina.cofolder.ui.main.MainViewModel
import com.dronina.cofolder.utils.extensions.*
import com.dronina.cofolder.utils.other.FOLDER
import com.dronina.cofolder.utils.other.GRID
import com.dronina.cofolder.utils.other.SORT_BUNDLE
import com.dronina.cofolder.utils.other.VERTICAL
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ImagesFragment : BaseFragment(), FoldersRvAdapter.OnItemSelectedListener,
    SwipeRefreshLayout.OnRefreshListener {
    private lateinit var binding: FragmentImagesBinding
    private lateinit var viewModel: ImagesViewModel
    private lateinit var mainViewModel: MainViewModel
    private lateinit var insideFolderViewModel: InsideFolderViewModel
    private var recyclerViewAdapter: FoldersRvAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_images, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated() {
        showProgress()
    }

    override fun initViewModels() {
        viewModel = ViewModelProviders
            .of(this, BaseViewModelFactory(requireContext()))
            .get(ImagesViewModel::class.java)
        mainViewModel = ViewModelProviders
            .of(requireActivity(), BaseViewModelFactory(requireContext()))
            .get(MainViewModel::class.java)
        insideFolderViewModel = ViewModelProviders
            .of(requireActivity(), BaseViewModelFactory(requireContext()))
            .get(InsideFolderViewModel::class.java)
        binding.viewModel = viewModel
        viewModel.view = this
        recyclerViewAdapter = null
        mainViewModel.onCreate()
    }

    override fun addObservers() {
        mainViewModel.currentFolders.observe(viewLifecycleOwner, { folders ->
            viewModel.allFolders.value = folders as ArrayList<FolderFile>
            viewModel.sort()
        })
        viewModel.foldersSorted.observe(viewLifecycleOwner, {
            refresh()
        })
    }

    override fun addListeners() {
        binding.swipeToRefresh.setOnRefreshListener(this)
        binding.btnAddFolder.setOnClickListener {
            (requireActivity() as AppCompatActivity).supportActionBar?.title =
                getString(R.string.create_note)
            viewModel.addFolderClicked()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        inflater.inflate(R.menu.folders_menu, menu)
        setSearchView(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_add_folder -> {
                (requireActivity() as AppCompatActivity).supportActionBar?.title =
                    getString(R.string.create_folder)
                viewModel.addFolderClicked()
            }
            R.id.sub_menu -> {
                if (!viewModel.canClick) return false
                viewModel.canClick = false
                showMenuPopup(
                    viewModel.showAsGird(),
                    { viewModel.setShowAsGrid() },
                    { navigateSortPage() })
                GlobalScope.launch {
                    delay(500)
                    viewModel.canClick = true
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun refresh() {
        viewModel.foldersSorted.value?.let { foldersSorted ->
            hideProgress()
            binding.btnAddFolder.setVisibility(foldersSorted.size <= 0)
            if (recyclerViewAdapter == null) {
                setRecyclerView(foldersSorted)
            } else {
                recyclerViewAdapter?.submitList(foldersSorted)
            }
        }
    }

    fun setRecyclerView(foldersSorted: ArrayList<FolderFile>) {
        binding.rvFolders.setHasFixedSize(true)
        if (viewModel.showAsGird()) {
            binding.rvFolders.layoutManager =
                StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
            binding.rvFolders.setItemDecorations(GRID)
        } else {
            binding.rvFolders.layoutManager = LinearLayoutManager(requireContext())
            binding.rvFolders.setItemDecorations(VERTICAL)
        }
        recyclerViewAdapter = FoldersRvAdapter(this, viewModel.showAsGird())
        recyclerViewAdapter?.submitList(foldersSorted)
        binding.rvFolders.adapter = recyclerViewAdapter

        if (viewModel.scrollPosition != 0) {
            binding.rvFolders.scrollToPosition(viewModel.scrollPosition)
        }
        binding.rvFolders.animateViewAppearance(viewModel.openRvWithAnimation)
        viewModel.openRvWithAnimation = false

        binding.rvFolders.addSwipes(
            context = requireContext(),
            enabled = !viewModel.showAsGird(),
            onMove = { holder, target ->
                viewModel.onItemRangeChanged(holder.adapterPosition, target.adapterPosition)
                binding.rvFolders.adapter
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

    fun navigateEditPage(arguments: Bundle) {
        insideFolderViewModel.currentFolder.value = null
        insideFolderViewModel.images.value = null
        insideFolderViewModel.scrollPosition.value = null
        insideFolderViewModel.navigateWithoutTransition.value = true
        findNavController().navigate(R.id.action_images_to_insideFolder, arguments)
    }

    fun removeFromList(id: String) {
        mainViewModel.removeItem(FOLDER, id)
    }

    private fun navigateSortPage() {
        findNavController().navigate(
            R.id.action_images_to_sort,
            bundleOf(SORT_BUNDLE to FOLDER)
        )
    }

    fun showBottomSheet(bundle: Bundle) {
        findNavController().navigate(R.id.action_images_to_bottomSheet, bundle)
    }

    fun showConfirmationDialog(message: String) {
        viewModel.foldersSorted.value?.let { foldersSorted ->
            requireContext().showConfirmationDialog(
                message = message,
                positiveOnClick =  { viewModel.confirmed() },
                negativeOnClick = { setRecyclerView(foldersSorted) })
        }
    }

    override fun showProgress() {
        binding.progressBar.visibility = View.VISIBLE
    }

    override fun hideProgress() {
        binding.progressBar.visibility = View.GONE
    }

    override fun onShortClick(folder: FolderFile?) {
        (requireActivity() as AppCompatActivity).supportActionBar?.title = folder?.name
        viewModel.onShortClick(folder)
    }

    override fun onMenuClick(folder: FolderFile?): Boolean {
        return viewModel.onLongClick(folder)
    }

    private fun setSearchView(menu: Menu) {
        requireActivity().setSearchView(
            menu,
            R.id.action_search_folder,
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

    override fun onRefresh() {
        viewModel.scrollPosition = 0
        viewModel.openRvWithAnimation = true
        mainViewModel.refreshNotes()
        viewModel.foldersSorted.value?.let { foldersSorted -> setRecyclerView(foldersSorted) }
        binding.swipeToRefresh.isRefreshing = false
    }

    override fun onStop() {
        super.onStop()
        binding.rvFolders.layoutManager?.let { layoutManager ->
            if (viewModel.showAsGird()) {
                viewModel.scrollPosition =
                    (binding.rvFolders.layoutManager as StaggeredGridLayoutManager).findFirstVisibleItemPositions(
                        null
                    )[0]
            } else {
                viewModel.scrollPosition =
                    (binding.rvFolders.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition()
            }
        }
    }
}
