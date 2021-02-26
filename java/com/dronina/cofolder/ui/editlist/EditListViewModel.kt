package com.dronina.cofolder.ui.editlist

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dronina.cofolder.R
import com.dronina.cofolder.data.model.other.ListItem
import com.dronina.cofolder.data.model.entities.ListFile
import com.dronina.cofolder.data.repository.ListsRepository
import com.dronina.cofolder.data.room.CofolderDatabase
import com.dronina.cofolder.utils.other.BY_DEFAULT
import com.dronina.cofolder.utils.other.DATA_BUNDLE
import com.dronina.cofolder.utils.extensions.connected
import com.dronina.cofolder.utils.extensions.currentTime
import com.dronina.cofolder.utils.extensions.ifLet
import com.google.firebase.Timestamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class EditListViewModel(private val context: Context) : ViewModel(),
    ListsRepository.ListUpdateListener {
    private var repo: ListsRepository? = null
    var view: EditListFragment? = null
    var userIsEditor = MutableLiveData<Boolean>()
    val itemToAdd = MutableLiveData<String>()
    val currentList = MutableLiveData<ListFile>()
    val sorting = MutableLiveData<Int>()
    var currentListId: String? = null
    var canClick = true

    init {
        itemToAdd.value = ""
        sorting.value = BY_DEFAULT
        CofolderDatabase.getDatabase(context)?.listDao()?.let { dao ->
            repo = ListsRepository(dao)
        }
    }

    fun onViewCreated(arguments: Bundle?) {
        if (currentList.value == null) {
            currentList.value = arguments?.getParcelable(DATA_BUNDLE)
        }
        userIsEditor.value = repo?.isEditor(currentList.value)
        currentList.value?.name?.let { name -> view?.setName(name) }
        view?.setItems()
        currentList.value?.color?.let { color -> view?.drawBoarder(color) }
        val listItems = ArrayList<ListItem>()
        currentList.value?.let { list -> listItems.addAll(list.items) }
        val updatedList = currentList.value
        updatedList?.items = listItems
        currentList.value = updatedList
        currentListId = currentList.value?.id
        currentListId?.let { repo?.addListUpdateListener(this, it) }
    }

    fun checked(sortingMethod: Int) {
        this.sorting.value = sortingMethod
    }

    fun time(): Timestamp {
        return currentTime()
    }

    fun shareOnClick() {
        if (repo == null || currentList.value == null || !connected()) {
            view?.networkError()
        } else ifLet(repo, currentList.value) { (repo, list) ->
            if (connected()) view?.navigateSharePage(
                (repo as ListsRepository).createBundle(null, list as ListFile)
            )
        }
    }

    fun showAsCreator(): Boolean {
        return if (repo == null || currentList.value == null) false
        else repo?.let { repo ->
            currentList.value?.let { list -> repo.isCreator(list) }
        } ?: run { false }
    }

    fun confirmed() {
        ifLet(repo, currentList.value) { (repo, list) ->
            view?.removeItemFromList()
            viewModelScope.launch {
                if (showAsCreator()) {
                    withContext(Dispatchers.IO) {
                        (repo as ListsRepository).delete(list as ListFile)
                    }
                } else withContext(Dispatchers.IO) {
                    (repo as ListsRepository).leave(list as ListFile)
                }
            }
        }
    }

    fun deleteOrLeave() {
        if (showAsCreator()) {
            view?.showConfirmationDialog(
                context.getString(R.string.confirm_delete_start) + " \"" + currentList.value?.name + "\"? " + context.getString(
                    R.string.confirm_delete_end
                )
            )
        } else view?.showConfirmationDialog(
            context.getString(R.string.confirm_leave_start) + " \"" + currentList.value?.name + "\"? " + context.getString(
                R.string.confirm_leave_end
            )
        )
    }

    fun nameUpdated(newName: String) {
        ifLet(currentList.value, userIsEditor.value) { (list, isEditor) ->
            if (!(isEditor as Boolean) || (list as ListFile).name == newName) return
            list.name = newName
            currentList.value = list
            viewModelScope.launch {
                withContext(Dispatchers.IO) { repo?.updateName(list, newName) }
            }
        }
    }

    fun colorUpdated(color: Int) {
        ifLet(currentList.value, userIsEditor.value) { (list, isEditor) ->
            if (!(isEditor as Boolean) || (list as ListFile).color == color) return
            view?.removeBoarder(list.color)
            view?.drawBoarder(color)
            list.color = color
            currentList.value = list
            viewModelScope.launch {
                withContext(Dispatchers.IO) { repo?.updateColor(list, color) }
            }
        }
    }

    override fun listUpdated(list: ListFile) {
        syncName(list)
        syncColor(list)
        syncContents(list)
        syncContributors(list)
        syncEditors(list)
        view?.updateMainModel()
    }

    override fun listUpdatedByMe(list: ListFile) {
        syncContributors(list)
        syncEditors(list)
        view?.updateMainModel()
    }

    fun addItemClicked() {
        currentList.value?.let { list ->
            if (itemToAdd.value.isNullOrEmpty() ||
                list.items.find { it.text == itemToAdd.value } != null
            ) return
            itemToAdd.value?.let { list.items.add(ListItem(it, false)) }
            updateListItemsAsync(list.items, false)
            currentList.value = list
            itemToAdd.value = ""
        }
    }

    fun removeItemClicked(item: ListItem) {
        currentList.value?.let { list ->
            val listItems = ArrayList<ListItem>()
            listItems.addAll(list.items)
            listItems.remove(item)
            updateListItemsAsync(listItems, true)
            val updatedList = currentList.value
            updatedList?.items = listItems
            currentList.value = updatedList
        }
    }

    fun onCheckedChanged(item: ListItem, checked: Boolean) {
        currentList.value?.let { list ->
            val listItems = ArrayList<ListItem>()
            listItems.addAll(list.items)
            listItems.find { it.text == item.text }?.checked = checked
            updateListItemsAsync(listItems, true)
        }
    }

    fun itemEdited(item: ListItem, newText: String) {
        currentList.value?.let { list ->
            if (newText.isEmpty() ||
                list.items.find { it.text == newText } != null
            ) return
            list.items.forEach {
                if (it.text == item.text) it.text = newText
            }
            updateListItemsAsync(list.items, false)
            currentList.value = list
        }
    }

    fun onItemRangeChanged(startPos: Int, endPos: Int) {
        currentList.value?.let { list ->
            Collections.swap(list.items, startPos, endPos)
            updateListItemsAsync(list.items, false)
            currentList.value?.items = list.items
        }
    }

    private fun syncContents(list: ListFile) {
        currentList.value?.items?.let { currentItems ->
            if (currentItems == list.items) return@syncContents
            currentList.value?.items = list.items
            view?.setItems()
        }
    }

    private fun syncName(list: ListFile) {
        currentList.value?.name?.let { currentName ->
            if (currentName == list.name) return@syncName
            view?.let { view ->
                var cursorPosition = view.getNameCursor()
                if (list.lastEditNamePosition <= cursorPosition) {
                    cursorPosition = list.name.length - abs(cursorPosition - currentName.length)
                    currentList.value?.name = list.name
                    this.view?.setName(list.name)
                    val finalPosition = min(max(0, cursorPosition), list.name.length)
                    this.view?.setNameCursor(finalPosition)
                } else {
                    this.view?.setName(list.name)
                    this.view?.setNameCursor(cursorPosition)
                }
            }
        }
    }

    private fun syncColor(list: ListFile) {
        currentList.value?.color?.let { currentColor ->
            if (currentColor == list.color) return@syncColor
            view?.removeBoarder(currentColor)
            view?.drawBoarder(list.color)
            view?.setColor(list.color)
        }
    }

    private fun syncContributors(list: ListFile) {
        currentList.value?.contributors?.let { currentContributors ->
            currentList.value?.contributors = list.contributors
        }
    }

    private fun syncEditors(list: ListFile) {
        currentList.value?.editors?.let { currentEditors ->
            currentList.value?.editors = list.editors
        }
    }

    private fun updateListItemsAsync(items: List<ListItem>, notifyAdapter: Boolean) {
        ifLet(currentList.value, userIsEditor.value) { (list, isEditor) ->
            if (!(isEditor as Boolean)) return
            viewModelScope.launch {
                withContext(Dispatchers.IO) { repo?.updateItems(list as ListFile, items) }
                if (notifyAdapter) {
                    view?.setItems()
                }
            }
        }
    }
}