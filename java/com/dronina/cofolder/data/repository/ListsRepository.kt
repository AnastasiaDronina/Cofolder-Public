package com.dronina.cofolder.data.repository

import android.os.Bundle
import com.dronina.cofolder.data.firebase.FirebaseSource
import com.dronina.cofolder.data.model.other.ListItem
import com.dronina.cofolder.data.model.entities.ListFile
import com.dronina.cofolder.data.model.entities.User
import com.dronina.cofolder.data.room.dao.ListDao
import com.dronina.cofolder.utils.extensions.connected
import com.dronina.cofolder.utils.extensions.currentTime
import com.dronina.cofolder.utils.extensions.formatListItems
import com.dronina.cofolder.utils.other.*
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await

class ListsRepository(private val dao: ListDao) : BaseRepository() {
    private var listUpdateListener: ListUpdateListener? = null
    private var currentlySendingDataToBackend: Boolean = false

    interface ListUpdateListener {
        fun listUpdated(list: ListFile)
        fun listUpdatedByMe(list: ListFile)
    }

    interface UserUpdateListener {
        fun listsUpdated(lists: List<String>)
    }

    fun addListUpdateListener(listener: ListUpdateListener, id: String) {
        listUpdateListener = listener
        try {
            FirebaseSource.firestoreRef(LIST, id)?.addSnapshotListener { snapshot, e ->
                if (snapshot == null || !snapshot.exists()) return@addSnapshotListener
                val updatedList = snapshot.toObject<ListFile>()
                if (updatedList == null || updatedList.offline) return@addSnapshotListener
                if (updatedList.lastUserEdited == FirebaseSource.userId()) {
                    listUpdateListener?.listUpdatedByMe(updatedList)
                } else if (!currentlySendingDataToBackend) {
                    listUpdateListener?.listUpdated(updatedList)
                }
            }
        } catch (e: Exception) {
        }
    }

    suspend fun updateName(list: ListFile, name: String) {
        return if (list.offline) {
            dao.updateName(list.id, name)
        } else {
            currentlySendingDataToBackend = true
            if (connected()) {
                list.name = name
                save(list.id, list)
            } else {
                FirebaseSource.firestoreRef(LIST, list.id)?.update(NAME_FIELD, name).await()
            }
            currentlySendingDataToBackend = false
        }
    }

    suspend fun updateItems(list: ListFile, items: List<ListItem>) {
        return if (list.offline) {
            dao.updateItems(list.id, items.formatListItems())
        } else {
            currentlySendingDataToBackend = true
            if (connected()) {
                list.items = items as java.util.ArrayList<ListItem>
                save(list.id, list)
            } else {
                FirebaseSource.firestoreRef(LIST, list.id)?.update(ITEMS_FIELD, items).await()
            }
            currentlySendingDataToBackend = false
        }
    }

    suspend fun updateColor(list: ListFile, color: Int) {
        return if (list.offline) {
            dao.updateColor(list.id, color)
        } else {
            currentlySendingDataToBackend = true
            if (connected()) {
                list.color = color
                save(list.id, list)
            } else {
                FirebaseSource.firestoreRef(LIST, list.id)?.update(COLOR_FIELD, color).await()
            }
            currentlySendingDataToBackend = false
        }
    }

    suspend fun save(id: String, list: ListFile): Boolean {
        return if (super.save(LIST, id, list)) {
            true
        } else {
            list.offline = true
            try {
                dao.insert(list)
                true
            } catch (e: Exception) {
                false
            }
        }
    }

    suspend fun delete(list: ListFile): Boolean {
        return if (super.delete(LIST, list)) {
            true
        } else try {
            dao.delete(list)
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun leave(list: ListFile): Boolean {
        return super.leave(LIST, list)
    }

    fun sort(list: ArrayList<ListFile>): ArrayList<ListFile> {
        return super.sort(LIST, list)
    }

    fun showAsGrid(): Boolean {
        return super.showAsGrid(LIST)
    }

    fun changeGridOrList() {
        return super.changeGridOrList(LIST)
    }

    override fun createUniqueId(): String {
        return "list_" + super.createUniqueId()
    }

    suspend fun getLists(ids: List<String>): ArrayList<ListFile>? {
        return super.getListOf<ListFile>(LIST, ids).addOfflineLists()
    }

    fun createBundle(touchPosition: Pair<Int, Int>?, list: ListFile): Bundle {
        val bundle = super.createBundle(LIST, list)
        bundle.putParcelable(LIST_BUNDLE, list)
        touchPosition?.let {
            bundle.putInt(X_POSITION, touchPosition.first)
            bundle.putInt(Y_POSITION, touchPosition.second)
        }
        return bundle
    }

    suspend fun updateRange(lists: ArrayList<ListFile>) {
        val listsIds = ArrayList<String>()
        lists.forEach { list ->
            listsIds.add(list.id)
        }
        FirebaseSource.currentUserObject()?.let { user ->
            user.lists = listsIds
            FirebaseSource.firestoreRef(USER, user.id)?.set(user)?.await()
        }
    }

    fun createNewList(id: String, name: String, items: ArrayList<ListItem>, color: Int): ListFile? {
        return FirebaseSource.userId()?.let { userId ->
            ListFile(
                id = id,
                name = name,
                creator = userId,
                dateOfLastEdit = currentTime(),
                contributors = listOf(userId),
                color = color,
                offline = false,
                items = items,
                lastEditNamePosition = 0,
                lastUserEdited = userId
            )
        }
    }

    private suspend fun ArrayList<ListFile>.addOfflineLists(): ArrayList<ListFile> {
        val offlineLists = dao.getAll()
        return if (offlineLists.isEmpty()) this
        else {
            if (!connected()) {
                this.addAll(offlineLists)
            } else {
                offlineLists.forEach { list ->
                    list.offline = false
                    val savedOnServerSuccessfully = save(list.id, list)
                    if (savedOnServerSuccessfully) {
                        list.offline = true
                        dao.delete(list)
                    }
                    this.add(list)
                }
            }
            this
        }
    }
}