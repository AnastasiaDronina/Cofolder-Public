package com.dronina.cofolder.ui.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.dronina.cofolder.R
import com.dronina.cofolder.databinding.BottomSheetBinding
import com.dronina.cofolder.ui.base.BaseViewModelFactory
import com.dronina.cofolder.ui.insidefolder.InsideFolderViewModel
import com.dronina.cofolder.ui.main.MainViewModel
import com.dronina.cofolder.utils.extensions.getThemeForDialog
import com.dronina.cofolder.utils.extensions.showConfirmationDialog
import com.dronina.cofolder.utils.extensions.whichTheme
import com.dronina.cofolder.utils.other.DARK_THEME
import com.dronina.cofolder.utils.other.FOLDER
import com.dronina.cofolder.utils.other.LIST
import com.dronina.cofolder.utils.other.NOTE
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class BottomSheetFragment : BottomSheetDialogFragment() {
    private lateinit var binding: BottomSheetBinding
    private lateinit var viewModel: BottomSheetViewModel
    private lateinit var mainViewModel: MainViewModel
    private lateinit var insideFolderViewModel: InsideFolderViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.bottom_sheet, container, false)
        return binding.root
    }

    override fun getTheme(): Int {
        return requireActivity().getThemeForDialog()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViewModels()
        addListeners()

        viewModel.onViewCreated(arguments)
        showAsCreator(viewModel.isCreator)
    }

    fun showConfirmationDialog(message: String) {
        requireContext().showConfirmationDialog(
            message = message,
            positiveOnClick = { viewModel.confirmed() })
    }

    fun navigateSharePage() {
        findNavController().navigate(R.id.action_bottomSheet_to_share, arguments)
    }

    fun networkError() {
        mainViewModel.networkError()
    }

    fun removeFromList() {
        viewModel.dataType?.let { dataType ->
            viewModel.data?.let { data ->
                mainViewModel.removeItem(dataType, data.id)
            }
        }
    }

    fun refreshList() {
        mainViewModel.onCreate()
    }

    private fun initViewModels() {
        viewModel = ViewModelProviders
            .of(requireActivity(), BaseViewModelFactory(requireContext()))
            .get(BottomSheetViewModel::class.java)
        mainViewModel = ViewModelProviders
            .of(requireActivity(), BaseViewModelFactory(requireContext()))
            .get(MainViewModel::class.java)
        insideFolderViewModel = ViewModelProviders
            .of(requireActivity(), BaseViewModelFactory(requireContext()))
            .get(InsideFolderViewModel::class.java)
        viewModel.view = this
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
    }

    private fun addListeners() {
        binding.btnLeaveOrDelete.setOnClickListener {
            viewModel.leaveOrDelete()
            findNavController().navigateUp()
        }
        binding.btnEdit.setOnClickListener {
            when (viewModel.dataType) {
                NOTE -> findNavController().navigate(
                    R.id.action_bottomSheet_to_editNote,
                    arguments
                )
                LIST -> findNavController().navigate(
                    R.id.action_bottomSheet_to_editList,
                    arguments
                )
                FOLDER -> {
                    insideFolderViewModel.navigateWithoutTransition.value = true
                    findNavController().navigate(
                        R.id.action_bottomSheet_to_insideFolder,
                        arguments
                    )
                }
            }
        }
        binding.btnShare.setOnClickListener {
            viewModel.shareOnClick()
        }
    }

    private fun showAsCreator(isCreator: Boolean) {
        val delete = requireContext().resources.getDrawable(R.drawable.ic_delete)
        val leave = requireContext().resources.getDrawable(R.drawable.ic_leave)
        delete.setTint(getDrawableColor())
        leave.setTint(getDrawableColor())
        if (isCreator) {
            binding.btnLeaveOrDelete.setText(R.string.delete)
            binding.btnLeaveOrDelete.setCompoundDrawablesWithIntrinsicBounds(
                delete,
                null,
                null,
                null
            )
        } else {
            binding.btnLeaveOrDelete.setText(R.string.leave)
            binding.btnLeaveOrDelete.setCompoundDrawablesWithIntrinsicBounds(
                leave,
                null,
                null,
                null
            )
        }
    }

    private fun getDrawableColor(): Int {
        return when (requireContext().whichTheme()) {
            DARK_THEME -> R.color.nightOnSurface
            else -> R.color.colorOnSurface
        }
    }
}
