package com.dronina.cofolder.ui.changetheme

import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.dronina.cofolder.R
import com.dronina.cofolder.databinding.FragmentChangeThemeBinding
import com.dronina.cofolder.ui.base.BaseFragment
import com.dronina.cofolder.ui.base.BaseViewModelFactory
import com.dronina.cofolder.utils.other.DARK_THEME
import com.dronina.cofolder.utils.other.DEFAULT_THEME
import com.dronina.cofolder.utils.other.LIGHT_THEME

class ChangeThemeFragment : BaseFragment() {
    private lateinit var binding: FragmentChangeThemeBinding
    private lateinit var viewModel: ChangeThemeViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_change_theme, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun initViewModels() {
        viewModel = ViewModelProviders
            .of(this, BaseViewModelFactory(requireContext()))
            .get(ChangeThemeViewModel::class.java)
    }

    override fun addObservers() {
        viewModel.currentTheme.observe(viewLifecycleOwner, {
            if (viewModel.currentTheme.value != null) {
                when (viewModel.currentTheme.value) {
                    LIGHT_THEME -> binding.rgThemes.check(R.id.rb_light_theme)
                    DARK_THEME -> binding.rgThemes.check(R.id.rb_dark_theme)
                    DEFAULT_THEME -> binding.rgThemes.check(R.id.rb_default_theme)
                }
            }
        })
    }

    override fun addListeners() {
        binding.rgThemes.setOnCheckedChangeListener { radioGroup, checkedId ->
            when (checkedId) {
                R.id.rb_light_theme -> viewModel.checked(LIGHT_THEME)
                R.id.rb_dark_theme -> viewModel.checked(DARK_THEME)
                R.id.rb_default_theme -> viewModel.checked(DEFAULT_THEME)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.save, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_save -> {
                viewModel.saveTheme()
                navigateUp()
                super.onOptionsItemSelected(item)
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
