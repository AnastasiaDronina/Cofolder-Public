package com.dronina.cofolder.ui.base

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.dronina.cofolder.R
import com.dronina.cofolder.utils.extensions.toast

open class BaseFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return onCreateView(inflater, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onViewCreated()
        initViewModels()
        addObservers()
        addListeners()
    }

    open fun onCreateView(inflater: LayoutInflater, container: ViewGroup?): View? = null

    open fun initViewModels() {}

    open fun addObservers() {}

    open fun addListeners() {}

    open fun onViewCreated() {}

    open fun hideProgress() {}

    open fun showProgress() {}

    open fun refresh() {}

    fun navigateUp() {
        findNavController().navigateUp()
    }

    fun networkError() {
        requireActivity().toast(getString(R.string.network_error))
    }
}