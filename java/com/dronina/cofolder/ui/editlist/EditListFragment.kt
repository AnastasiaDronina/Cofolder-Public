package com.dronina.cofolder.ui.editlist

import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dronina.cofolder.R
import com.dronina.cofolder.data.model.other.ListItem
import com.dronina.cofolder.data.model.entities.ListFile
import com.dronina.cofolder.databinding.FragmentEditListBinding
import com.dronina.cofolder.ui.base.BaseFragment
import com.dronina.cofolder.ui.base.BaseViewModelFactory
import com.dronina.cofolder.ui.lists.ListsViewModel
import com.dronina.cofolder.ui.main.MainViewModel
import com.dronina.cofolder.utils.extensions.*
import com.dronina.cofolder.utils.other.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class EditListFragment : BaseFragment(), ListItemsRvAdapter.OnItemClickListener {
    private lateinit var binding: FragmentEditListBinding
    private lateinit var viewModel: EditListViewModel
    private lateinit var mainViewModel: MainViewModel
    private lateinit var listsViewModel: ListsViewModel

    private var recyclerViewAdapter: ListItemsRvAdapter? = null
    private var bottomSheetBehavior: BottomSheetBehavior<*>? = null
    private var colorPalette: LinearLayout? = null
    private var etListName: EditText? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_edit_list, container, false)
        setHasOptionsMenu(true)
        binding.root.animateCircularReveal(requireActivity(), arguments, { arguments == null })
        return binding.root
    }

    override fun onViewCreated() {
        setPageTitle()
        setBottomSheet()
    }

    override fun initViewModels() {
        viewModel = ViewModelProviders
            .of(this, BaseViewModelFactory(requireContext()))
            .get(EditListViewModel::class.java)
        listsViewModel = ViewModelProviders
            .of(requireActivity(), BaseViewModelFactory(requireContext()))
            .get(ListsViewModel::class.java)
        mainViewModel = ViewModelProviders
            .of(requireActivity(), BaseViewModelFactory(requireContext()))
            .get(MainViewModel::class.java)
        viewModel.view = this
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        recyclerViewAdapter = null
        viewModel.onViewCreated(arguments)
    }

    override fun addObservers() {
        viewModel.currentList.observe(viewLifecycleOwner, {
            viewModel.currentList.value?.let {
                recyclerViewAdapter?.sort()
                setItems()
            }
        })
        viewModel.sorting.observe(viewLifecycleOwner, {
            recyclerViewAdapter?.let { adapter ->
                adapter.sorting = viewModel.sorting.value
                adapter.sort()
            }
        })
        viewModel.userIsEditor.observe(viewLifecycleOwner, { userIdEditor ->
            binding.clAddItem.setVisibility(userIdEditor)
        })
    }

    override fun addListeners() {
        colorPalette?.setColorListener { view ->
            ifLet(view?.colorTag(), viewModel.currentList.value) { (color, list) ->
                viewModel.colorUpdated(color as Int)
                listsViewModel.colorUpdated(list as ListFile, color, viewModel.time())
            }
        }
        etListName?.doOnTextChanged { text, start, before, count ->
            viewModel.currentList.value?.let { list ->
                (requireActivity() as AppCompatActivity).supportActionBar?.title = text.toString()
                viewModel.nameUpdated(text.toString())
                listsViewModel.nameUpdated(list, text.toString(), viewModel.time())
            }
        }
        binding.etAddItem.setOnEditorActionListener { view, actionId, event ->
            if (actionId != EditorInfo.IME_ACTION_SEND) {
                false
            } else {
                viewModel.currentList.value?.let { list ->
                    viewModel.addItemClicked()
                    listsViewModel.itemsUpdated(list, list.items, viewModel.time())
                    true
                } ?: run { false }
            }
        }
        binding.etAddItem.doOnTextChanged { text, start, before, count ->
            binding.appBarLayout.setExpanded(true)
        }

        binding.btnAddItem.setOnClickListener {
            viewModel.currentList.value?.let { list ->
                requireActivity().showKeyboard(binding.etAddItem)
                viewModel.addItemClicked()
                listsViewModel.itemsUpdated(list, list.items, viewModel.time())
            }
        }
        viewModel.userIsEditor.value?.let { userIdEditor ->
            if (userIdEditor) {
                requireActivity().findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
                    .setOnClickListener { bottomSheetBehavior?.hideOrExpand() }
            }

        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        inflater.inflate(R.menu.edit_list_menu, menu)
        viewModel.userIsEditor.value?.let { userIsEditor ->
            if (!userIsEditor) {
                menu.removeItem(R.id.settings)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.settings -> bottomSheetBehavior?.hideOrExpand()
            R.id.sort -> showSortDialog()
            R.id.sub_menu -> if (viewModel.canClick) {
                viewModel.canClick = false
                showEditPageMenu(
                    viewModel.showAsCreator(),
                    { viewModel.deleteOrLeave() },
                    { viewModel.shareOnClick() }
                )
                GlobalScope.launch {
                    delay(500)
                    viewModel.canClick = true
                }
            }
            android.R.id.home -> {
                binding.etAddItem.hideKeyboardFrom(requireContext())
                etListName?.hideKeyboardFrom(requireContext())
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun setItems() {
        ifLet(viewModel.currentList.value, viewModel.currentList.value?.items) { (list, items) ->
            recyclerViewAdapter?.let {
                recyclerViewAdapter?.update(items as ArrayList<ListItem>)
            } ?: run {
                refresh()
            }
        }
    }

    fun updateMainModel() {
        mainViewModel.onCreate()
    }

    fun navigateSharePage(bundle: Bundle) {
        findNavController().navigate(R.id.action_editList_to_share, bundle)
    }

    fun removeItemFromList() {
        viewModel.currentList.value?.let { list ->
            val lists = mainViewModel.currentLists.value
            (lists as ArrayList<ListFile>).remove(list)
            mainViewModel.currentLists.value = lists
        }
        findNavController().navigateUp()
    }

    fun removeBoarder(color: Int) {
        colorPalette?.removeBoarder(color)
    }

    fun drawBoarder(color: Int) {
        colorPalette?.drawBoarder(color)
    }

    fun setName(name: String) {
        try {
            viewModel.userIsEditor.value?.let { userIsEditor ->
                if (userIsEditor) {
                    etListName?.setText(name)
                }
            }
            (activity as AppCompatActivity).supportActionBar?.title = name
        } catch (e: Exception) {
        }
    }

    fun setColor(color: Int) {
        listsViewModel.colorUpdated(viewModel.currentList.value, color, viewModel.time())
    }

    fun getNameCursor(): Int {
        return etListName?.let { editText ->
            editText.selectionEnd
        } ?: run { 0 }
    }

    fun setNameCursor(position: Int) {
        etListName?.let { editText ->
            if (position >= 0 && position <= editText.text.length) {
                etListName?.setSelection(position)
            }
        }
    }

    fun showConfirmationDialog(message: String) {
        requireContext().showConfirmationDialog(
            message = message,
            positiveOnClick = { viewModel.confirmed() })
    }

    override fun showProgress() {
        binding.progressBar.visibility = View.VISIBLE
    }

    override fun hideProgress() {
        binding.progressBar.visibility = View.GONE
    }

    override fun onCheckedChanged(listItem: ListItem, checked: Boolean) {
        viewModel.onCheckedChanged(listItem, checked)
        viewModel.currentList.value?.let { list ->
            listsViewModel.itemsUpdated(list, list.items, viewModel.time())
        }
    }

    override fun onEditItemClicked(listItem: ListItem) {
        requireActivity().showEditTextDialog(
            "",
            "",
            listItem.text,
            { viewModel.itemEdited(listItem, it) }
        )
    }

    override fun onRemoveItemClicked(listItem: ListItem) {
        viewModel.removeItemClicked(listItem)
        viewModel.currentList.value?.let { list ->
            listsViewModel.itemsUpdated(list, list.items, viewModel.time())
        }
    }

    override fun refresh() {
        binding.rvListItems.setHasFixedSize(true)
        binding.rvListItems.layoutManager = LinearLayoutManager(requireContext())
        viewModel.currentList.value?.let { list ->
            recyclerViewAdapter = viewModel.userIsEditor.value?.let {
                ListItemsRvAdapter(it, layoutInflater, list.items, this, viewModel.sorting.value)
            }
            binding.rvListItems.setItemDecorations(VERTICAL)
            binding.rvListItems.adapter = recyclerViewAdapter
            viewModel.userIsEditor.value?.let { userIsEditor ->
                binding.rvListItems.addSwipes(
                    context = requireContext(),
                    enabled = userIsEditor,
                    onMove = { holder, target ->
                        viewModel.onItemRangeChanged(holder.adapterPosition, target.adapterPosition)
                        binding.rvListItems.adapter
                            ?.notifyItemMoved(holder.adapterPosition, target.adapterPosition)
                    },
                    onSwiped = { holder ->
                        recyclerViewAdapter?.let { adapter ->
                            onRemoveItemClicked(adapter.getSortedItemsInCorrectRange()[holder.adapterPosition])
                        }
                    }
                )
            }
        }
    }

    private fun showSortDialog() {
        val view = layoutInflater.inflate(R.layout.sort_list_items, null)
        val radioGroup = view.findViewById<RadioGroup>(R.id.rg_sort_methods)
        when (viewModel.sorting.value) {
            BY_DEFAULT -> radioGroup.check(R.id.rb_by_default)
            CHECKED_FIRST -> radioGroup.check(R.id.rb_checked_first)
            UNCHECKED_FIRST -> radioGroup.check(R.id.rb_unchecked_first)
            ALPHABETICALLY -> radioGroup.check(R.id.rb_alphabetically)
        }
        requireActivity().showCustomDialog(
            view = view,
            title = getString(R.string.sort),
            positiveOnClick = {
                when (radioGroup.checkedRadioButtonId) {
                    R.id.rb_by_default -> viewModel.checked(BY_DEFAULT)
                    R.id.rb_checked_first -> viewModel.checked(CHECKED_FIRST)
                    R.id.rb_unchecked_first -> viewModel.checked(UNCHECKED_FIRST)
                    R.id.rb_alphabetically -> viewModel.checked(ALPHABETICALLY)
                }
            })
    }

    private fun setPageTitle() {
        arguments?.let {
            (requireActivity() as AppCompatActivity).supportActionBar?.title =
                arguments?.getParcelable<ListFile>(DATA_BUNDLE)?.name
        } ?: run {
            (requireActivity() as AppCompatActivity).supportActionBar?.title =
                getString(R.string.create_list)
        }
    }

    private fun setBottomSheet() {
        val bottomSheet =
            requireView().handleBottomSheet(editTextHint = requireContext().getString(R.string.list_name))
        bottomSheetBehavior = bottomSheet.second
        colorPalette = bottomSheet.first.findViewById(R.id.linear_layout)
        etListName = bottomSheet.first.findViewById(R.id.et_name)
    }
}