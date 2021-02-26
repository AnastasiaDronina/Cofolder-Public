package com.dronina.cofolder.ui.language

import android.view.*
import android.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.dronina.cofolder.R
import com.dronina.cofolder.databinding.FragmentLanguageBinding
import com.dronina.cofolder.ui.base.BaseFragment
import com.dronina.cofolder.ui.base.BaseViewModelFactory
import com.dronina.cofolder.utils.extensions.setItemDecorations
import com.dronina.cofolder.utils.extensions.setSearchView
import com.dronina.cofolder.utils.extensions.showConfirmationDialog
import com.dronina.cofolder.utils.other.VERTICAL

class LanguageFragment : BaseFragment(), LanguagesRvAdapter.OnItemSelectedListener {
    private lateinit var binding: FragmentLanguageBinding
    private lateinit var viewModel: LanguageViewModel
    private var recyclerViewAdapter: LanguagesRvAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_language, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated() {
        recyclerViewAdapter = null
    }

    override fun initViewModels() {
        viewModel = ViewModelProviders
            .of(this, BaseViewModelFactory(requireContext()))
            .get(LanguageViewModel::class.java)
        viewModel.view = this
    }

    override fun addObservers() {
        viewModel.languages.observe(viewLifecycleOwner, {
            refresh()
        })
    }

    override fun refresh() {
        viewModel.languages.value?.let { languages ->
            recyclerViewAdapter?.let {
                recyclerViewAdapter?.update(languages)
            } ?: run {
                binding.rvLanguages.setHasFixedSize(true)
                binding.rvLanguages.layoutManager = LinearLayoutManager(requireContext())
                binding.rvLanguages.setItemDecorations(VERTICAL)
                recyclerViewAdapter = LanguagesRvAdapter(languages, this)
                binding.rvLanguages.adapter = recyclerViewAdapter
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        inflater.inflate(R.menu.menu_just_search, menu)
        setSearchView(menu)
    }

    private fun setSearchView(menu: Menu) {
        requireActivity().setSearchView(
            menu,
            R.id.action_search,
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

    override fun onShortClick(language: String?) {
        requireActivity().showConfirmationDialog(
            message = getString(R.string.confirm_lang_change) + " $language?",
            positiveOnClick = { viewModel.changeLanguage(language) }
        )
    }

    fun recreateActivity() {
        requireActivity().recreate()
    }

}
