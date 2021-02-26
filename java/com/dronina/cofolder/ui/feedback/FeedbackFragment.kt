package com.dronina.cofolder.ui.feedback

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.dronina.cofolder.R
import com.dronina.cofolder.databinding.FragmentFeedbackBinding
import com.dronina.cofolder.ui.base.BaseFragment
import com.dronina.cofolder.ui.base.BaseViewModelFactory
import com.dronina.cofolder.utils.extensions.toast

class FeedbackFragment : BaseFragment() {
    private lateinit var binding: FragmentFeedbackBinding
    private lateinit var viewModel: FeedbackViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_feedback, container, false)
        return binding.root
    }

    override fun initViewModels() {
        viewModel = ViewModelProviders
            .of(requireActivity(), BaseViewModelFactory(requireContext()))
            .get(FeedbackViewModel::class.java)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        viewModel.view = this
    }

    override fun addListeners() {
        binding.btnSendIssue.setOnClickListener {
            viewModel.sendIssue(binding.etFeedback.text.toString())
        }
    }

    override fun showProgress() {
        binding.progressBar.visibility = View.VISIBLE
    }

    override fun hideProgress() {
        binding.progressBar.visibility = View.GONE
    }

    fun feedbackSent() {
        requireActivity().toast(getString(R.string.issue_was_sent))
        binding.etFeedback.setText("")
    }
}
