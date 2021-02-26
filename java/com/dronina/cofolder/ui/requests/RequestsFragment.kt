package com.dronina.cofolder.ui.requests

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dronina.cofolder.R
import com.dronina.cofolder.data.model.entities.Request
import com.dronina.cofolder.databinding.FragmentRequestsBinding
import com.dronina.cofolder.ui.base.BaseFragment
import com.dronina.cofolder.ui.base.BaseViewModelFactory
import com.dronina.cofolder.ui.main.MainViewModel
import com.dronina.cofolder.utils.extensions.setItemDecorations
import com.dronina.cofolder.utils.extensions.setShow
import com.dronina.cofolder.utils.extensions.setVisibility
import com.dronina.cofolder.utils.other.VERTICAL

class RequestsFragment : BaseFragment(), OnItemClickListener {
    private lateinit var binding: FragmentRequestsBinding
    private lateinit var viewModel: RequestsViewModel
    private lateinit var mainViewModel: MainViewModel
    private var toMeAdapter: ToMeRvAdapter? = null
    private var byMeAdapter: ByMeRvAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_requests, container, false)
        return binding.root
    }

    override fun onViewCreated() {
        toMeAdapter = null
        byMeAdapter = null
    }

    override fun initViewModels() {
        viewModel = ViewModelProviders
            .of(requireActivity(), BaseViewModelFactory(requireContext()))
            .get(RequestsViewModel::class.java)
        mainViewModel = ViewModelProviders
            .of(requireActivity(), BaseViewModelFactory(requireContext()))
            .get(MainViewModel::class.java)
        viewModel.view = this
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
    }

    override fun addListeners() {
        binding.btnShowSentToMe.setOnClickListener {
            viewModel.showRequestsToMeOnClick()
        }
        binding.btnShowSentByMe.setOnClickListener {
            viewModel.showRequestsByMeOnClick()
        }
    }

    override fun addObservers() {
        mainViewModel.currentRequests.observe(viewLifecycleOwner, {
            refresh()
        })
        viewModel.showRequestsToMe.observe(viewLifecycleOwner, { showRequestsToMe ->
            if (showRequestsToMe) {
                binding.rvToMe.visibility = View.VISIBLE
                mainViewModel.currentRequests.value?.let { currentRequests ->
                    binding.tvNoRequestsToMe.setVisibility(currentRequests.first.isEmpty())
                }
            } else {
                binding.rvToMe.visibility = View.GONE
                binding.tvNoRequestsToMe.visibility = View.GONE
            }
            binding.btnShowSentToMe.setShow(showRequestsToMe)
        })
        viewModel.showRequestsByMe.observe(viewLifecycleOwner, { showRequestsByMe ->
            if (showRequestsByMe) {
                binding.rvByMe.visibility = View.VISIBLE
                mainViewModel.currentRequests.value?.let { currentRequests ->
                    binding.tvNoRequestsByMe.setVisibility(currentRequests.second.isEmpty())
                }
            } else {
                binding.rvByMe.visibility = View.GONE
                binding.tvNoRequestsByMe.visibility = View.GONE
            }
            binding.btnShowSentByMe.setShow(showRequestsByMe)
        })
    }

    override fun refresh() {
        mainViewModel.currentRequests.value?.let { currentRequests ->
            if (toMeAdapter == null) {
                setToMeRecyclerView()
            } else {
                toMeAdapter?.update(currentRequests.first as ArrayList<Request>)
            }
            if (byMeAdapter == null) {
                setByBeRecyclerView()
            } else {
                byMeAdapter?.update(currentRequests.second as ArrayList<Request>)
            }
            binding.tvNoRequestsToMe.setVisibility(currentRequests.first.isEmpty())
            binding.tvNoRequestsByMe.setVisibility(currentRequests.second.isEmpty())
        }
    }

    override fun onItemClick(request: Request?) {
        viewModel.onShortClick(request)
    }

    override fun cancelOnClick(request: Request?, position: Int) {
        byMeAdapter?.notifyItemRemoved(position)
        viewModel.cancelRequestOnClick(request)
    }

    override fun acceptOnClick(request: Request?, position: Int) {
        toMeAdapter?.notifyItemRemoved(position)
        viewModel.acceptRequestOnClick(request)
    }

    fun navigatePublicProfile(pageTitle: String, bundle: Bundle) {
        (requireActivity() as AppCompatActivity).supportActionBar?.title = pageTitle
        findNavController().navigate(R.id.action_requests_to_publicProfile, bundle)
    }

    private fun setToMeRecyclerView() {
        mainViewModel.currentRequests.value?.let { currentRequests ->
            binding.rvToMe.setHasFixedSize(true)
            binding.rvToMe.layoutManager = LinearLayoutManager(requireContext())
            toMeAdapter = ToMeRvAdapter(currentRequests.first as ArrayList<Request>, this)
            binding.rvToMe.setItemDecorations(VERTICAL)
            binding.rvToMe.adapter = toMeAdapter
            binding.tvNoRequestsToMe.setVisibility(currentRequests.first.isEmpty())
        }
    }

    private fun setByBeRecyclerView() {
        mainViewModel.currentRequests.value?.let { currentRequests ->
            binding.rvByMe.setHasFixedSize(true)
            binding.rvByMe.layoutManager = LinearLayoutManager(requireContext())
            byMeAdapter = ByMeRvAdapter(currentRequests.second as ArrayList<Request>, this)
            binding.rvByMe.setItemDecorations(VERTICAL)
            binding.rvByMe.adapter = byMeAdapter
            binding.tvNoRequestsByMe.setVisibility(currentRequests.second.isEmpty())
        }
    }
}
