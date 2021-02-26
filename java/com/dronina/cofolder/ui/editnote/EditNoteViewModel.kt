package com.dronina.cofolder.ui.editnote

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dronina.cofolder.R
import com.dronina.cofolder.data.model.entities.NoteFile
import com.dronina.cofolder.data.repository.NotesRepository
import com.dronina.cofolder.data.room.CofolderDatabase
import com.dronina.cofolder.utils.other.DATA_BUNDLE
import com.dronina.cofolder.utils.extensions.connected
import com.dronina.cofolder.utils.extensions.currentTime
import com.dronina.cofolder.utils.extensions.ifLet
import com.google.firebase.Timestamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class EditNoteViewModel(private val context: Context) : ViewModel(),
    NotesRepository.NoteUpdateListener {
    private var repo: NotesRepository? = null
    var view: EditNoteFragment? = null
    var userIsEditor = MutableLiveData<Boolean>()
    var currentNote = MutableLiveData<NoteFile>()
    var canEdit = MutableLiveData<Boolean>()
    var currentNoteId: String? = null
    var canClick = true

    init {
        canEdit.value = true
        CofolderDatabase.getDatabase(context)?.noteDao()?.let { dao ->
            repo = NotesRepository(dao)
        }
    }

    fun onViewCreated(arguments: Bundle?) {
        if (currentNote.value == null) {
            currentNote.value = arguments?.getParcelable(DATA_BUNDLE)
        }
        userIsEditor.value = repo?.isEditor(currentNote.value)
        currentNote.value?.name?.let { name -> view?.setName(name) }
        currentNote.value?.text?.let { text -> view?.setText(text) }
        currentNote.value?.color?.let { color -> view?.drawBoarder(color) }
        currentNoteId = currentNote.value?.id
        currentNoteId?.let { id -> repo?.addNoteUpdateListener(this, id) }
    }

    fun time(): Timestamp {
        return currentTime()
    }

    fun shareOnClick() {
        if (repo == null || currentNote.value == null || !connected()) {
            view?.networkError()
        } else ifLet(repo, currentNote.value) { (repo, note) ->
            if (connected()) view?.navigateSharePage(
                (repo as NotesRepository).createBundle(null, note as NoteFile)
            )
        }
    }

    fun showAsCreator(): Boolean {
        return if (repo == null || currentNote.value == null) false
        else repo?.let { repo ->
            currentNote.value?.let { note -> repo.isCreator(note) }
        } ?: run { false }
    }

    fun confirmed() {
        ifLet(repo, currentNote.value) { (repo, note) ->
            view?.removeNoteFromList()
            viewModelScope.launch {
                if (showAsCreator()) {
                    withContext(Dispatchers.IO) {
                        (repo as NotesRepository).delete(note as NoteFile)
                    }
                } else withContext(Dispatchers.IO) {
                    (repo as NotesRepository).leave(note as NoteFile)
                }
            }
        }
    }

    fun deleteOrLeave() {
        if (showAsCreator()) {
            view?.showConfirmationDialog(
                context.getString(R.string.confirm_delete_start) + " \"" + currentNote.value?.name + "\"? " + context.getString(
                    R.string.confirm_delete_end
                )
            )
        } else {
            view?.showConfirmationDialog(
                context.getString(R.string.confirm_leave_start) + " \"" + currentNote.value?.name + "\"? " + context.getString(
                    R.string.confirm_leave_end
                )
            )
        }
    }

    fun nameUpdated(newName: String) {
        ifLet(currentNote.value, userIsEditor.value, view) { (note, isEditor, view) ->
            if (!(isEditor as Boolean) || (note as NoteFile).name == newName) return
            note.name = newName
            currentNote.value = note
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    repo?.updateName(note, newName, (view as EditNoteFragment).getNameCursor())
                }
            }
        }
    }

    fun colorUpdated(color: Int) {
        ifLet(currentNote.value, userIsEditor.value) { (note, isEditor) ->
            if (!(isEditor as Boolean) || (note as NoteFile).color == color) return
            view?.removeBoarder(note.color)
            view?.drawBoarder(color)
            note.color = color
            currentNote.value = note
            viewModelScope.launch {
                withContext(Dispatchers.IO) { repo?.updateColor(note, color) }
            }
        }
    }

    fun contentsUpdated(newText: String) {
        ifLet(currentNote.value, userIsEditor.value, view) { (note, isEditor, view) ->
            if (!(isEditor as Boolean) || (note as NoteFile).text == newText) return
            note.text = newText
            currentNote.value = note
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    repo?.updateText(note, newText, (view as EditNoteFragment).getTextCursor())
                }
            }
        }
    }


    override fun noteUpdated(note: NoteFile) {
        syncName(note)
        syncColor(note)
        syncContents(note)
        syncContributors(note)
        syncEditors(note)
        view?.updateMainModel()
    }

    override fun noteUpdatedByMe(note: NoteFile) {
        syncContributors(note)
        syncEditors(note)
        view?.updateMainModel()
    }

    private fun syncContents(note: NoteFile) {
        currentNote.value?.text?.let { currentText ->
            if (currentText == note.text) return
            view?.let { view ->
                var cursorPosition = view.getTextCursor()
                if (note.lastEditTextPosition <= cursorPosition) {
                    cursorPosition = note.text.length - abs(cursorPosition - currentText.length)
                    currentNote.value?.text = note.text
                    view.setText(note.text)
                    val finalPosition = min(max(0, cursorPosition), note.text.length)
                    view.setTextCursor(finalPosition)
                } else {
                    view.setText(note.text)
                    view.setTextCursor(cursorPosition)
                }
            }
        }
    }

    private fun syncName(note: NoteFile) {
        currentNote.value?.name?.let { currentName ->
            if (currentName == note.name) return
            view?.let { view ->
                var cursorPosition = view.getNameCursor()
                if (note.lastEditNamePosition <= cursorPosition) {
                    cursorPosition = note.name.length - abs(cursorPosition - currentName.length)
                    currentNote.value?.name = note.name
                    view.setName(note.name)
                    val finalPosition = min(max(0, cursorPosition), note.name.length)
                    view.setNameCursor(finalPosition)
                } else {
                    view.setName(note.name)
                    view.setNameCursor(cursorPosition)
                }
            }
        }
    }

    private fun syncColor(note: NoteFile) {
        currentNote.value?.color?.let { currentColor ->
            if (currentColor == note.color) return
            view?.removeBoarder(currentColor)
            view?.drawBoarder(note.color)
            view?.setColor(note.color)
        }
    }

    private fun syncContributors(note: NoteFile) {
        currentNote.value?.contributors?.let { currentContributors ->
            currentNote.value?.contributors = note.contributors
        }
    }

    private fun syncEditors(note: NoteFile) {
        currentNote.value?.editors?.let { currentEditors ->
            currentNote.value?.editors = note.editors
        }
    }
}