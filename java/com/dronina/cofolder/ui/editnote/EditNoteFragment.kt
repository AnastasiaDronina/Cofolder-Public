package com.dronina.cofolder.ui.editnote

import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.dronina.cofolder.R
import com.dronina.cofolder.data.model.entities.NoteFile
import com.dronina.cofolder.databinding.FragmentEditNoteBinding
import com.dronina.cofolder.ui.base.BaseFragment
import com.dronina.cofolder.ui.base.BaseViewModelFactory
import com.dronina.cofolder.ui.main.MainViewModel
import com.dronina.cofolder.ui.notes.NotesViewModel
import com.dronina.cofolder.utils.extensions.*
import com.dronina.cofolder.utils.other.DATA_BUNDLE
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class EditNoteFragment : BaseFragment() {
    private lateinit var binding: FragmentEditNoteBinding
    private lateinit var viewModel: EditNoteViewModel
    private lateinit var mainViewModel: MainViewModel
    private lateinit var notesViewModel: NotesViewModel

    private var bottomSheetBehavior: BottomSheetBehavior<*>? = null
    private var colorPalette: LinearLayout? = null
    private var etNoteName: EditText? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_edit_note, container, false)
        setHasOptionsMenu(true)
        binding.root.animateCircularReveal(requireActivity(), arguments, { arguments == null })
        return binding.root
    }

    override fun onViewCreated() {
        setPageTitle()
        setBottomSheet()
    }

    override fun initViewModels() {
        viewModel = ViewModelProviders
            .of(this, BaseViewModelFactory(requireContext()))
            .get(EditNoteViewModel::class.java)
        notesViewModel = ViewModelProviders
            .of(requireActivity(), BaseViewModelFactory(requireContext()))
            .get(NotesViewModel::class.java)
        mainViewModel = ViewModelProviders
            .of(requireActivity(), BaseViewModelFactory(requireContext()))
            .get(MainViewModel::class.java)
        viewModel.view = this
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        viewModel.onViewCreated(arguments)
    }

    override fun addObservers() {
        viewModel.canEdit.observe(viewLifecycleOwner, { canEdit ->
            binding.etNoteText.isFocusable = canEdit
            binding.etNoteText.isFocusableInTouchMode = canEdit
        })

        viewModel.userIsEditor.observe(viewLifecycleOwner, { isEditor ->
            binding.etNoteText.setVisibility(isEditor)
            binding.tvNoteText.setVisibility(!isEditor)
        })
    }

    override fun addListeners() {
        colorPalette?.setColorListener { view ->
            ifLet(view?.colorTag(), viewModel.currentNote.value) { (color, note) ->
                viewModel.colorUpdated(color as Int)
                notesViewModel.colorUpdated(note as NoteFile, color, viewModel.time())
            }
        }
        binding.etNoteText.doOnTextChanged { text, start, before, count ->
            viewModel.contentsUpdated(text.toString())
            notesViewModel.contentsUpdated(
                viewModel.currentNote.value,
                text.toString(),
                viewModel.time()
            )
        }
        etNoteName?.doOnTextChanged { text, start, before, count ->
            (requireActivity() as AppCompatActivity).supportActionBar?.title = text.toString()
            viewModel.nameUpdated(text.toString())
            notesViewModel.nameUpdated(
                viewModel.currentNote.value,
                text.toString(),
                viewModel.time()
            )
        }
        viewModel.userIsEditor.value?.let { userIdEditor ->
            if (userIdEditor) {
                requireActivity().findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
                    .setOnClickListener { bottomSheetBehavior?.hideOrExpand() }
            }

        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        inflater.inflate(R.menu.edit_page_menu, menu)
        viewModel.userIsEditor.value?.let { userIsEditor ->
            if (!userIsEditor) {
                menu.removeItem(R.id.settings)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.settings -> bottomSheetBehavior?.hideOrExpand()
            android.R.id.home -> binding.etNoteText.hideKeyboardFrom(requireContext())
            R.id.sub_menu -> {
                if (!viewModel.canClick) return false
                viewModel.canClick = false
                showEditPageMenu(
                    viewModel.showAsCreator(),
                    { viewModel.deleteOrLeave() },
                    { viewModel.shareOnClick() }
                )
                GlobalScope.launch {
                    delay(500)
                    viewModel.canClick = true
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setBottomSheet() {
        val bottomSheet = requireView().handleBottomSheet(
            editTextHint = requireContext().getString(R.string.note_name),
            stateExpanded = { viewModel.canEdit.value = false },
            stateHidden = { viewModel.canEdit.value = true }
        )
        bottomSheetBehavior = bottomSheet.second
        colorPalette = bottomSheet.first.findViewById(R.id.linear_layout)
        etNoteName = bottomSheet.first.findViewById(R.id.et_name)
    }

    private fun setPageTitle() {
        arguments?.let {
            (requireActivity() as AppCompatActivity).supportActionBar?.title =
                arguments?.getParcelable<NoteFile>(DATA_BUNDLE)?.name
        } ?: run {
            (requireActivity() as AppCompatActivity).supportActionBar?.title =
                getString(R.string.create_note)
        }
    }

    fun removeNoteFromList() {
        viewModel.currentNote.value?.let { currentNote ->
            val notes = mainViewModel.currentNotes.value
            (notes as ArrayList<NoteFile>).remove(currentNote)
            mainViewModel.currentNotes.value = notes
        }
        findNavController().navigateUp()
    }

    fun showConfirmationDialog(message: String) {
        requireContext().showConfirmationDialog(
            message = message,
            positiveOnClick = { viewModel.confirmed() })
    }

    fun removeBoarder(color: Int) {
        colorPalette?.removeBoarder(color)
    }

    fun drawBoarder(color: Int) {
        colorPalette?.drawBoarder(color)
    }

    fun setText(text: String) {
        viewModel.userIsEditor.value?.let { userIsEditor ->
            if (userIsEditor) {
                binding.etNoteText.setText(text)
            } else {
                binding.tvNoteText.text = text
            }
        }
    }

    fun setName(name: String) {
        try {
            viewModel.userIsEditor.value?.let { userIsEditor ->
                if (userIsEditor) {
                    etNoteName?.setText(name)
                }
            }
            (activity as AppCompatActivity).supportActionBar?.title = name
        } catch (e: Exception) {
        }
    }

    fun setColor(color: Int) {
        notesViewModel.colorUpdated(
            viewModel.currentNote.value,
            color,
            viewModel.time()
        )
    }

    fun getTextCursor(): Int {
        return binding.etNoteText.selectionEnd
    }

    fun getNameCursor(): Int {
        return etNoteName?.let { editText ->
            editText.selectionEnd
        } ?: run { 0 }
    }


    fun setTextCursor(position: Int) {
        if (position >= 0 && position <= binding.etNoteText.text.length) {
            binding.etNoteText.setSelection(position)
        }
    }

    fun setNameCursor(position: Int) {
        etNoteName?.let { editText ->
            if (position >= 0 && position <= editText.text.length) {
                etNoteName?.setSelection(position)
            }
        }
    }

    fun updateMainModel() {
        mainViewModel.onCreate()
    }

    fun navigateSharePage(bundle: Bundle) {
        findNavController().navigate(R.id.action_editNote_to_share, bundle)
    }

    override fun showProgress() {
        binding.progressBar.visibility = View.VISIBLE
    }

    override fun hideProgress() {
        binding.progressBar.visibility = View.GONE
    }
}