package com.dronina.cofolder.ui.about

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.dronina.cofolder.R
import com.dronina.cofolder.databinding.FragmentAboutBinding
import com.dronina.cofolder.ui.base.BaseFragment
import com.dronina.cofolder.utils.extensions.rateApp

class AboutFragment : BaseFragment(), View.OnClickListener {
    private lateinit var binding: FragmentAboutBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?): View {
        binding = DataBindingUtil
            .inflate(inflater, R.layout.fragment_about, container, false)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun addListeners() {
        binding.btnFeedback.setOnClickListener(this)
        binding.btnPrivacyPolicy.setOnClickListener(this)
        binding.btnTerms.setOnClickListener(this)
        binding.btnRateApp.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        when (view) {
            binding.btnFeedback -> findNavController().navigate(R.id.action_about_to_feedback)
            binding.btnPrivacyPolicy -> findNavController().navigate(R.id.action_about_to_privacyPolicy)
            binding.btnTerms -> findNavController().navigate(R.id.action_about_to_terms)
            binding.btnRateApp -> requireContext().rateApp()
        }
    }
}
