package com.dronina.cofolder.ui.lists

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dronina.cofolder.R
import com.dronina.cofolder.data.model.other.ListItem
import com.dronina.cofolder.data.model.entities.ListFile
import com.dronina.cofolder.data.preferences.PreferenceManager
import com.dronina.cofolder.data.repository.ListsRepository
import com.dronina.cofolder.data.room.CofolderDatabase
import com.dronina.cofolder.data.room.dao.ListDao
import com.dronina.cofolder.utils.extensions.ifLet
import com.dronina.cofolder.utils.other.BY_DEFAULT
import com.dronina.cofolder.utils.other.DATA_BUNDLE
import com.dronina.cofolder.utils.other.LIST
import com.dronina.cofolder.utils.other.NO_COLOR
import com.google.firebase.Timestamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.collections.ArrayList

class ListsViewModel(private val context: Context) : ViewModel(),
    ListsRepository.UserUpdateListener {
    private var repo: ListsRepository? = null
    private var dao: ListDao? = null
    var view: ListsFragment? = null
    var allLists = MutableLiveData<ArrayList<ListFile>>()
    var listsSorted = MutableLiveData<ArrayList<ListFile>>()
    var listToDeleteOrLeave = MutableLiveData<ListFile>()
    var canClick = true
    var openRvWithAnimation = false
    var scrollPosition = 0

    init {
        CofolderDatabase.getDatabase(context)?.listDao()?.let { dao ->
            this.dao = dao
            repo = ListsRepository(dao)
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
        listsSorted.value?.let { listsSorted -> view?.setRecyclerView(listsSorted) }
    }

    fun onItemClick(touchPosition: Pair<Int, Int>, list: ListFile?) {
        if (!canClick) return
        ifLet(repo, list) { (repo, list) ->
            view?.navigateEditPage(
                (repo as ListsRepository).createBundle(touchPosition, list as ListFile)
            )
        }
    }

    fun onItemMenuClick(list: ListFile?): Boolean {
        return if (!canClick || repo == null || list == null) false
        else {
            repo?.let { repo ->
                view?.showBottomSheet(repo.createBundle(null, list))
            }
            true
        }
    }

    fun addListClicked() {
        if (!canClick) return
        repo?.let { repo ->
            val listId = repo.createUniqueId()
            repo.createNewList(listId, "", ArrayList(), NO_COLOR)?.let { list ->
                viewModelScope.launch {
                    withContext(Dispatchers.IO) {
                        repo.save(listId, list)
                    }
                }
                view?.navigateEditPage(createListBundle(list))
            }
        }
    }

    fun onItemRangeChanged(startPos: Int, endPos: Int) {
        PreferenceManager.setSorting(LIST, BY_DEFAULT)
        Collections.swap(listsSorted.value as ArrayList<ListFile>, startPos, endPos)
        val result = ArrayList<ListFile>()
        result.addAll(listsSorted.value as ArrayList<ListFile>)
        if (PreferenceManager.showOnlyPrivate(LIST)) {
            var publicLists = ArrayList<ListFile>()
            publicLists.addAll(allLists.value as ArrayList<ListFile>)
            publicLists = publicLists.filter { it.contributors.size > 1 } as ArrayList<ListFile>
            result.addAll(publicLists)
        }
        listsSorted.value?.let {
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    repo?.updateRange(result)
                }
            }
        }
    }

    fun confirmed() {
        ifLet(repo, listToDeleteOrLeave.value) { (repo, listToDeleteOrLeave) ->
            view?.removeFromList((listToDeleteOrLeave as ListFile).id)
            viewModelScope.launch {
                if ((repo as ListsRepository).isCreator(listToDeleteOrLeave)) {
                    if ((listToDeleteOrLeave as ListFile).offline) {
                        dao?.deleteById(listToDeleteOrLeave.id)
                    } else withContext(Dispatchers.IO) {
                        repo.delete(listToDeleteOrLeave)
                    }
                } else withContext(Dispatchers.IO) {
                    repo.leave(listToDeleteOrLeave as ListFile)
                }
            }
        }
    }

    fun isCreator(position: Int): Boolean? {
        return try {
            repo?.isCreator(listsSorted.value?.get(position))
        } catch (e: Exception) {
            null
        }
    }

    fun onSwiped(position: Int) {
        listToDeleteOrLeave.value = listsSorted.value?.get(position)
        repo?.let { repo ->
            if (repo.isCreator(listToDeleteOrLeave.value)) {
                view?.showConfirmationDialog(
                    context.getString(R.string.confirm_delete_start) + " \"" + listToDeleteOrLeave.value?.name + "\"? " + context.getString(
                        R.string.confirm_delete_end
                    )
                )
            } else {
                view?.showConfirmationDialog(
                    context.getString(R.string.confirm_leave_start) + " \"" + listToDeleteOrLeave.value?.name + "\"? " + context.getString(
                        R.string.confirm_leave_end
                    )
                )
            }
        }
    }

    fun sort() {
        allLists.value?.let { allLists ->
            listsSorted.value = repo?.sort(allLists)
        }
    }

    fun nameUpdated(updatedList: ListFile?, name: String, timestamp: Timestamp) {
        updatedList?.name = name
        updatedList?.dateOfLastEdit = timestamp
        updatedList?.let { sync(updatedList) }
    }

    fun colorUpdated(updatedList: ListFile?, color: Int, timestamp: Timestamp) {
        updatedList?.color = color
        updatedList?.dateOfLastEdit = timestamp
        updatedList?.let { sync(updatedList) }
    }

    fun itemsUpdated(updatedList: ListFile?, items: ArrayList<ListItem>, timestamp: Timestamp) {
        updatedList?.items = items
        updatedList?.dateOfLastEdit = timestamp
        updatedList?.let { sync(updatedList) }
    }

    override fun listsUpdated(lists: List<String>) {
        viewModelScope.launch {
            var listsArray: ArrayList<ListFile>? = null
            withContext(Dispatchers.IO) {
                listsArray = repo?.getLists(lists)
            }
            listsArray?.let {
                allLists.value = listsArray
                sort()
            }
        }
    }

    private fun sync(updatedList: ListFile) {
        allLists.value?.let { allLists ->
            val lists = ArrayList<ListFile>()
            allLists.forEach { list ->
                if (list.id == updatedList.id) {
                    lists.add(updatedList)
                } else {
                    lists.add(list)
                }
            }
            this.allLists.value = lists
            sort()
        }
    }

    private fun createListBundle(list: ListFile): Bundle {
        val bundle = Bundle()
        bundle.putParcelable(DATA_BUNDLE, list)
        return bundle
    }

}