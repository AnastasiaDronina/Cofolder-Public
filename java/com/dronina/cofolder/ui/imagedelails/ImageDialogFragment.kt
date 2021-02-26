package com.dronina.cofolder.ui.imagedelails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.dronina.cofolder.R
import com.dronina.cofolder.data.model.entities.User
import com.dronina.cofolder.databinding.FragmentImageDialogBinding
import com.dronina.cofolder.ui.base.BaseViewModelFactory
import com.dronina.cofolder.ui.main.MainViewModel
import com.dronina.cofolder.utils.extensions.getThemeForDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ImageDialogFragment : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentImageDialogBinding
    private lateinit var viewModel: ImageDetailsViewModel
    private lateinit var mainViewModel: MainViewModel

    override fun getTheme(): Int {
        return requireActivity().getThemeForDialog()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_image_dialog, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModels()
        addListeners()
        viewModel.bottomSheetCreated(arguments)
    }

    fun getImageCreator(creatorId: String): User? {
        return mainViewModel.findUser(creatorId)
    }

    fun showDelete() {
        binding.btnDelete.visibility = View.VISIBLE
    }

    fun hideDelete() {
        binding.btnDelete.visibility = View.GONE
    }

    fun setCreator(creator: String) {
        binding.tvCreator.text = creator
    }

    fun setDate(date: String) {
        binding.tvDate.text = date
    }

    private fun initViewModels() {
        viewModel = ViewModelProviders
            .of(requireActivity(), BaseViewModelFactory(requireContext()))
            .get(ImageDetailsViewModel::class.java)
        mainViewModel = ViewModelProviders
            .of(requireActivity(), BaseViewModelFactory(requireContext()))
            .get(MainViewModel::class.java)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        viewModel.bottomSheet = this
    }

    private fun addListeners() {
        binding.btnSaveToGallery.setOnClickListener {
            findNavController().navigateUp()
            viewModel.saveToGallery(requireActivity().contentResolver)
        }

        binding.btnDelete.setOnClickListener {
            findNavController().navigateUp()
            viewModel.delete()

        }
    }
}
