package com.dronina.cofolder.ui.settings

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.dronina.cofolder.R
import com.dronina.cofolder.databinding.FragmentSettingsBinding
import com.dronina.cofolder.ui.base.BaseFragment
import com.dronina.cofolder.ui.base.BaseViewModelFactory

class SettingsFragment : BaseFragment() {
    private lateinit var binding: FragmentSettingsBinding
    private lateinit var viewModel: SettingsViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_settings, container, false)
        return binding.root
    }

    override fun initViewModels() {
        viewModel = ViewModelProviders
            .of(requireActivity(), BaseViewModelFactory(requireContext()))
            .get(SettingsViewModel::class.java)
    }

    override fun addObservers() {
        viewModel.allowNotifications.observe(viewLifecycleOwner, { allow ->
            binding.switchNotifications.isChecked = allow
        })
    }

    override fun addListeners() {
        binding.btnTheme.setOnClickListener {
            findNavController().navigate(R.id.action_settings_to_changeTheme)
        }
        binding.btnLanguage.setOnClickListener {
            findNavController().navigate(R.id.action_settings_to_language)
        }

        binding.switchNotifications.setOnCheckedChangeListener { button, checked ->
            viewModel.checkedChanged(checked)
        }
    }

}
