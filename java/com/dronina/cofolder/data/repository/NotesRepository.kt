package com.dronina.cofolder.data.repository

import android.os.Bundle
import com.dronina.cofolder.data.firebase.FirebaseSource
import com.dronina.cofolder.data.model.entities.NoteFile
import com.dronina.cofolder.data.room.dao.NoteDao
import com.dronina.cofolder.utils.extensions.connected
import com.dronina.cofolder.utils.extensions.currentTime
import com.dronina.cofolder.utils.other.*
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await

class NotesRepository(private val dao: NoteDao) : BaseRepository() {
    private var noteUpdateListener: NoteUpdateListener? = null
    private var currentlySendingDataToBackend: Boolean = false

    interface NoteUpdateListener {
        fun noteUpdated(note: NoteFile)
        fun noteUpdatedByMe(note: NoteFile)
    }

    fun addNoteUpdateListener(listener: NoteUpdateListener, id: String) {
        noteUpdateListener = listener
        try {
            FirebaseSource.firestoreRef(NOTE, id)?.addSnapshotListener { snapshot, e ->
                if (snapshot == null || !snapshot.exists()) return@addSnapshotListener
                val updatedNote = snapshot.toObject<NoteFile>()
                if (updatedNote == null || updatedNote.offline) return@addSnapshotListener
                if (updatedNote.lastUserEdited == FirebaseSource.userId()) {
                    noteUpdateListener?.noteUpdatedByMe(updatedNote)
                } else if (!currentlySendingDataToBackend) {
                    noteUpdateListener?.noteUpdated(updatedNote)
                }
            }
        } catch (e: Exception) {
        }
    }

    suspend fun updateName(note: NoteFile, name: String, cursorPosition: Int) {
        return if (note.offline) {
            dao.updateName(note.id, name)
        } else {
            currentlySendingDataToBackend = true
            if (connected()) {
                note.name = name
                note.lastEditNamePosition = cursorPosition
                save(note.id, note)
            } else {
                FirebaseSource.firestoreRef(NOTE, note.id)?.update(NAME_FIELD, name).await()
            }
            currentlySendingDataToBackend = false
        }
    }

    suspend fun updateText(note: NoteFile, text: String, cursorPosition: Int) {
        if (note.offline) {
            dao.updateText(note.id, text)
        } else {
            currentlySendingDataToBackend = true
            if (connected()) {
                note.text = text
                note.lastEditTextPosition = cursorPosition
                save(note.id, note)
            } else {
                FirebaseSource.firestoreRef(NOTE, note.id)?.update(TEXT_FILED, text).await()
            }
            currentlySendingDataToBackend = false
        }
    }

    suspend fun updateColor(note: NoteFile, color: Int) {
        return if (note.offline) {
            dao.updateColor(note.id, color)
        } else {
            currentlySendingDataToBackend = true
            if (connected()) {
                note.color = color
                save(note.id, note)
            } else {
                FirebaseSource.firestoreRef(NOTE, note.id)?.update(COLOR_FIELD, color).await()
            }
            currentlySendingDataToBackend = false
        }
    }

    suspend fun save(id: String, note: NoteFile): Boolean {
        return if (super.save(NOTE, id, note)) {
            true
        } else {
            note.offline = true
            try {
                dao.insert(note)
                true
            } catch (e: Exception) {
                false
            }
        }
    }

    suspend fun delete(note: NoteFile): Boolean {
        return if (super.delete(NOTE, note)) {
            true
        } else try {
            dao.delete(note)
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun leave(note: NoteFile): Boolean {
        return super.leave(NOTE, note)
    }

    fun sort(list: ArrayList<NoteFile>): ArrayList<NoteFile> {
        return super.sort(NOTE, list)
    }

    fun showAsGrid(): Boolean {
        return super.showAsGrid(NOTE)
    }

    fun changeGridOrList() {
        return super.changeGridOrList(NOTE)
    }

    override fun createUniqueId(): String {
        return "note_" + super.createUniqueId()
    }

    suspend fun getNotes(ids: List<String>): ArrayList<NoteFile>? {
        return super.getListOf<NoteFile>(NOTE, ids).addOfflineNotes()
    }

    fun createBundle(touchPosition: Pair<Int, Int>?, note: NoteFile): Bundle {
        val bundle = super.createBundle(NOTE, note)
        bundle.putParcelable(NOTE_BUNDLE, note)
        touchPosition?.let {
            bundle.putInt(X_POSITION, touchPosition.first)
            bundle.putInt(Y_POSITION, touchPosition.second)
        }
        return bundle
    }

    suspend fun updateRange(notes: ArrayList<NoteFile>) {
        val notesIds = ArrayList<String>()
        notes.forEach { note ->
            notesIds.add(note.id)
        }
        FirebaseSource.currentUserObject()?.let { user ->
            user.notes = notesIds
            FirebaseSource.firestoreRef(USER, user.id)?.set(user)?.await()
        }
    }

    fun createNewNote(id: String, name: String, text: String, color: Int): NoteFile? {
        return FirebaseSource.userId()?.let { userId ->
            NoteFile(
                id = id,
                name = name,
                creator = userId,
                dateOfLastEdit = currentTime(),
                contributors = listOf(userId),
                color = color,
                offline = false,
                text = text,
                lastEditTextPosition = 0,
                lastEditNamePosition = 0,
                lastUserEdited = userId
            )
        }
    }

    private suspend fun ArrayList<NoteFile>.addOfflineNotes(): ArrayList<NoteFile> {
        val offlineNotes = dao.getAll()
        return if (offlineNotes.isEmpty()) this
        else {
            if (!connected()) {
                this.addAll(offlineNotes)
            } else {
                offlineNotes.forEach { note ->
                    note.offline = false
                    val savedOnServerSuccessfully = save(note.id, note)
                    if (savedOnServerSuccessfully) {
                        note.offline = true
                        dao.delete(note)
                    }
                    this.add(note)
                }
            }
            this
        }
    }
}