package com.dronina.cofolder.ui.notes

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dronina.cofolder.R
import com.dronina.cofolder.data.model.entities.NoteFile
import com.dronina.cofolder.data.preferences.PreferenceManager
import com.dronina.cofolder.data.repository.NotesRepository
import com.dronina.cofolder.data.room.CofolderDatabase
import com.dronina.cofolder.data.room.dao.NoteDao
import com.dronina.cofolder.utils.extensions.ifLet
import com.dronina.cofolder.utils.other.BY_DEFAULT
import com.dronina.cofolder.utils.other.NOTE
import com.dronina.cofolder.utils.other.NO_COLOR
import com.google.firebase.Timestamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.collections.ArrayList

class NotesViewModel(private val context: Context) : ViewModel() {
    private var repo: NotesRepository? = null
    private var dao: NoteDao? = null
    var view: NotesFragment? = null
    var allNotes = MutableLiveData<ArrayList<NoteFile>>()
    var notesSorted = MutableLiveData<ArrayList<NoteFile>>()
    var noteToDeleteOrLeave = MutableLiveData<NoteFile>()
    var canClick = true
    var openRvWithAnimation = false
    var scrollPosition = 0

    init {
        CofolderDatabase.getDatabase(context)?.noteDao()?.let { dao ->
            this.dao = dao
            repo = NotesRepository(dao)
        }
    }

    fun showAsGird(): Boolean {
        return repo?.let {
            repo?.showAsGrid()
        } ?: run {
            false
        }
    }

    fun setShowAsGrid() {
        repo?.changeGridOrList()
        openRvWithAnimation = true
        notesSorted.value?.let { notesSorted -> view?.setRecyclerView(notesSorted) }
    }

    fun onItemClick(touchPosition: Pair<Int, Int>, note: NoteFile?) {
        if (!canClick) return
        ifLet(repo, note) { (repo, note) ->
            view?.navigateEditPage(
                (repo as NotesRepository).createBundle(touchPosition, note as NoteFile)
            )
        }
    }

    fun onItemMenuClick(note: NoteFile?): Boolean {
        return if (!canClick || repo == null || note == null) false
        else {
            repo?.let { repo ->
                view?.showBottomSheet(repo.createBundle(null, note))
            }
            true
        }
    }

    fun addNoteClicked() {
        if (!canClick) return
        repo?.let { repo ->
            val noteId = repo.createUniqueId()
            repo.createNewNote(noteId, "", "", NO_COLOR)?.let { note ->
                viewModelScope.launch {
                    withContext(Dispatchers.IO) {
                        repo.save(noteId, note)
                    }
                }
                view?.navigateEditPage(repo.createBundle(null, note))
            }
        }
    }

    fun onItemRangeChanged(startPos: Int, endPos: Int) {
        PreferenceManager.setSorting(NOTE, BY_DEFAULT)
        Collections.swap(notesSorted.value as ArrayList<NoteFile>, startPos, endPos)
        val result = ArrayList<NoteFile>()
        result.addAll(notesSorted.value as ArrayList<NoteFile>)
        if (PreferenceManager.showOnlyPrivate(NOTE)) {
            var publicNotes = ArrayList<NoteFile>()
            publicNotes.addAll(allNotes.value as ArrayList<NoteFile>)
            publicNotes = publicNotes.filter { it.contributors.size > 1 } as ArrayList<NoteFile>
            result.addAll(publicNotes)
        }
        notesSorted.value?.let {
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    repo?.updateRange(result)
                }
            }
        }
    }

    fun confirmed() {
        ifLet(repo, noteToDeleteOrLeave.value) { (repo, noteToDeleteOrLeave) ->
            view?.removeFromList((noteToDeleteOrLeave as NoteFile).id)
            viewModelScope.launch {
                if ((repo as NotesRepository).isCreator(noteToDeleteOrLeave)) {
                    if ((noteToDeleteOrLeave as NoteFile).offline) {
                        dao?.deleteById(noteToDeleteOrLeave.id)
                    } else withContext(Dispatchers.IO) {
                        repo.delete(noteToDeleteOrLeave)
                    }
                } else withContext(Dispatchers.IO) {
                    repo.leave(noteToDeleteOrLeave as NoteFile)
                }
            }
        }
    }

    fun isCreator(position: Int): Boolean? {
        return try {
            repo?.isCreator(notesSorted.value?.get(position))
        } catch (e: Exception) {
            null
        }
    }


    fun onSwiped(position: Int) {
        noteToDeleteOrLeave.value = notesSorted.value?.get(position)
        repo?.let { repo ->
            if (repo.isCreator(noteToDeleteOrLeave.value)) {
                view?.showConfirmationDialog(
                    context.getString(R.string.confirm_delete_start) + " \"" + noteToDeleteOrLeave.value?.name + "\"? " + context.getString(
                        R.string.confirm_delete_end
                    )
                )
            } else {
                view?.showConfirmationDialog(
                    context.getString(R.string.confirm_leave_start) + " \"" + noteToDeleteOrLeave.value?.name + "\"? " + context.getString(
                        R.string.confirm_leave_end
                    )
                )
            }
        }
    }

    fun sort() {
        allNotes.value?.let { allNotes ->
            notesSorted.value = repo?.sort(allNotes)
        }
    }

    fun nameUpdated(updatedNote: NoteFile?, name: String, timestamp: Timestamp) {
        updatedNote?.name = name
        updatedNote?.dateOfLastEdit = timestamp
        updatedNote?.let { sync(updatedNote) }
    }

    fun colorUpdated(updatedNote: NoteFile?, color: Int, timestamp: Timestamp) {
        updatedNote?.color = color
        updatedNote?.dateOfLastEdit = timestamp
        updatedNote?.let { sync(updatedNote) }
    }

    fun contentsUpdated(updatedNote: NoteFile?, text: String, timestamp: Timestamp) {
        updatedNote?.text = text
        updatedNote?.dateOfLastEdit = timestamp
        updatedNote?.let { sync(updatedNote) }
    }

    private fun sync(updatedNote: NoteFile) {
        allNotes.value?.let { allNotes ->
            val notes = ArrayList<NoteFile>()
            allNotes.forEach { note ->
                if (note.id == updatedNote.id) {
                    notes.add(updatedNote)
                } else {
                    notes.add(note)
                }
            }
            this.allNotes.value = notes
            sort()
        }
    }
}