package com.dronina.cofolder.ui.sort

import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.dronina.cofolder.R
import com.dronina.cofolder.databinding.FragmentSortBinding
import com.dronina.cofolder.ui.base.BaseFragment
import com.dronina.cofolder.ui.base.BaseViewModelFactory
import com.dronina.cofolder.ui.images.ImagesViewModel
import com.dronina.cofolder.ui.lists.ListsViewModel
import com.dronina.cofolder.ui.notes.NotesViewModel
import com.dronina.cofolder.utils.other.*

class SortFragment : BaseFragment() {
    private lateinit var binding: FragmentSortBinding
    private lateinit var viewModel: SortViewModel
    private lateinit var notesViewModel: NotesViewModel
    private lateinit var listsViewModel: ListsViewModel
    private lateinit var imagesViewModel: ImagesViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_sort, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun initViewModels() {
        viewModel = ViewModelProviders
            .of(this, BaseViewModelFactory(requireContext()))
            .get(SortViewModel::class.java)
        notesViewModel = ViewModelProviders
            .of(requireActivity(), BaseViewModelFactory(requireContext()))
            .get(NotesViewModel::class.java)
        listsViewModel = ViewModelProviders
            .of(requireActivity(), BaseViewModelFactory(requireContext()))
            .get(ListsViewModel::class.java)
        imagesViewModel = ViewModelProviders
            .of(requireActivity(), BaseViewModelFactory(requireContext()))
            .get(ImagesViewModel::class.java)
        viewModel.view = this
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        viewModel.onViewCreated(requireArguments())
    }

    override fun addListeners() {
        binding.rgSortMethods.setOnCheckedChangeListener { rg, checkedId ->
            when (checkedId) {
                R.id.rb_by_default -> viewModel.checked(BY_DEFAULT)
                R.id.rb_by_date -> viewModel.checked(BY_DATE)
                R.id.rb_by_color -> viewModel.checked(BY_COLOR)
                R.id.rb_alphabetically -> viewModel.checked(ALPHABETICALLY)
            }
        }
        binding.cbShowOnlyPrivate.setOnCheckedChangeListener { button, checked ->
            viewModel.showOnlyPrivate(checked)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.save, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_save -> viewModel.saveSort()
        }
        return super.onOptionsItemSelected(item)
    }

    fun setRadioButtonChecked(sorting: Int) {
        when (sorting) {
            BY_DEFAULT -> binding.rgSortMethods.check(R.id.rb_by_default)
            BY_DATE -> binding.rgSortMethods.check(R.id.rb_by_date)
            BY_COLOR -> binding.rgSortMethods.check(R.id.rb_by_color)
            ALPHABETICALLY -> binding.rgSortMethods.check(R.id.rb_alphabetically)
        }
    }

    fun setCheckbox(checked: Boolean) {
        binding.cbShowOnlyPrivate.isChecked = checked
    }

    fun updateSorting(dataType: Int) {
        when (dataType) {
            NOTE -> notesViewModel.sort()
            LIST -> listsViewModel.sort()
            FOLDER -> imagesViewModel.sort()
        }
    }

}
