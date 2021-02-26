package com.dronina.cofolder.ui.notes

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
import com.dronina.cofolder.data.model.entities.NoteFile
import com.dronina.cofolder.databinding.FragmentNotesBinding
import com.dronina.cofolder.ui.base.BaseFragment
import com.dronina.cofolder.ui.base.BaseViewModelFactory
import com.dronina.cofolder.ui.main.MainViewModel
import com.dronina.cofolder.utils.extensions.*
import com.dronina.cofolder.utils.other.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class NotesFragment : BaseFragment(), NotesRvAdapter.OnItemClickListener,
    SwipeRefreshLayout.OnRefreshListener {
    private lateinit var binding: FragmentNotesBinding
    private lateinit var viewModel: NotesViewModel
    private lateinit var mainViewModel: MainViewModel
    private var recyclerViewAdapter: NotesRvAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_notes, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated() {
        doNothingOnBackPressed()
        showProgress()
    }

    override fun initViewModels() {
        viewModel = ViewModelProviders
            .of(requireActivity(), BaseViewModelFactory(requireContext()))
            .get(NotesViewModel::class.java)
        mainViewModel = ViewModelProviders
            .of(requireActivity(), BaseViewModelFactory(requireContext()))
            .get(MainViewModel::class.java)
        viewModel.view = this
        recyclerViewAdapter = null
        mainViewModel.onCreate()
    }

    override fun addListeners() {
        binding.btnAddNote.setOnClickListener {
            (requireActivity() as AppCompatActivity).supportActionBar?.title =
                getString(R.string.create_note)
            viewModel.addNoteClicked()
        }
        binding.swipeToRefresh.setOnRefreshListener(this)
    }

    override fun addObservers() {
        mainViewModel.currentNotes.observe(viewLifecycleOwner, { notes ->
            viewModel.allNotes.value = notes as ArrayList<NoteFile>
            viewModel.sort()
        })
        viewModel.notesSorted.observe(viewLifecycleOwner, {
            refresh()
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        inflater.inflate(R.menu.notes_menu, menu)
        setSearchView(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_add_note -> {
                (requireActivity() as AppCompatActivity).supportActionBar?.title =
                    getString(R.string.create_note)
                viewModel.addNoteClicked()
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
        viewModel.notesSorted.value?.let { notesSorted ->
            hideProgress()
            binding.btnAddNote.setVisibility(notesSorted.size <= 0)
            if (recyclerViewAdapter == null) {
                setRecyclerView(notesSorted)
            } else {
                recyclerViewAdapter?.submitList(notesSorted)
            }
        }
    }

    fun setRecyclerView(notesSorted: ArrayList<NoteFile>) {
        binding.rvNotes.setHasFixedSize(true)
        if (viewModel.showAsGird()) {
            binding.rvNotes.layoutManager =
                StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
            binding.rvNotes.setItemDecorations(GRID)
        } else {
            binding.rvNotes.layoutManager = LinearLayoutManager(requireContext())
            binding.rvNotes.setItemDecorations(VERTICAL)
        }
        recyclerViewAdapter = NotesRvAdapter(this, viewModel.showAsGird())
        recyclerViewAdapter?.submitList(notesSorted)
        binding.rvNotes.adapter = recyclerViewAdapter

        if (viewModel.scrollPosition != 0) {
            binding.rvNotes.scrollToPosition(viewModel.scrollPosition)
        }
        binding.rvNotes.animateViewAppearance(viewModel.openRvWithAnimation)
        viewModel.openRvWithAnimation = false

        binding.rvNotes.addSwipes(
            context = requireContext(),
            enabled = !viewModel.showAsGird(),
            onMove = { holder, target ->
                viewModel.onItemRangeChanged(holder.adapterPosition, target.adapterPosition)
                binding.rvNotes.adapter
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
        mainViewModel.removeItem(NOTE, id)
    }

    fun navigateEditPage(arguments: Bundle) {
        findNavController().navigate(R.id.action_notes_to_editNote, arguments)
    }

    fun showBottomSheet(bundle: Bundle) {
        findNavController().navigate(R.id.action_notes_to_bottomSheet, bundle)
    }

    fun showConfirmationDialog(message: String) {
        viewModel.notesSorted.value?.let { notesSorted ->
            requireContext().showConfirmationDialog(
                message = message,
                positiveOnClick = { viewModel.confirmed() },
                negativeOnClick = { setRecyclerView(notesSorted) })
        }
    }

    override fun onItemClick(touchPosition: Pair<Int, Int>, note: NoteFile?) {
        (requireActivity() as AppCompatActivity).supportActionBar?.title = note?.name
        viewModel.onItemClick(touchPosition, note)
    }

    override fun onItemMenuClick(note: NoteFile?): Boolean {
        return viewModel.onItemMenuClick(note)
    }

    override fun onRefresh() {
        viewModel.scrollPosition = 0
        viewModel.openRvWithAnimation = true
        mainViewModel.refreshNotes()
        viewModel.notesSorted.value?.let { notesSorted -> setRecyclerView(notesSorted) }
        binding.swipeToRefresh.isRefreshing = false
    }

    override fun onStop() {
        super.onStop()
        binding.rvNotes.layoutManager?.let { layoutManager ->
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
        findNavController().navigate(R.id.action_notes_to_sort, bundleOf(SORT_BUNDLE to NOTE))
    }

    private fun setSearchView(menu: Menu) {
        activity?.setSearchView(
            menu,
            R.id.action_search_note,
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
