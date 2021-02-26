package com.dronina.cofolder.ui.images

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dronina.cofolder.R
import com.dronina.cofolder.data.model.entities.FolderFile
import com.dronina.cofolder.data.preferences.PreferenceManager
import com.dronina.cofolder.data.repository.ImagesRepository
import com.dronina.cofolder.data.room.CofolderDatabase
import com.dronina.cofolder.data.room.dao.FolderDao
import com.dronina.cofolder.utils.extensions.ifLet
import com.dronina.cofolder.utils.other.BY_DEFAULT
import com.dronina.cofolder.utils.other.FOLDER
import com.google.firebase.Timestamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.collections.ArrayList

class ImagesViewModel(private val context: Context) : ViewModel() {
    private var repo: ImagesRepository? = null
    private var dao: FolderDao? = null
    var view: ImagesFragment? = null
    var allFolders = MutableLiveData<ArrayList<FolderFile>>()
    var foldersSorted = MutableLiveData<ArrayList<FolderFile>>()
    var folderToDeleteOrLeave = MutableLiveData<FolderFile>()
    var canClick = true
    var openRvWithAnimation = false
    var scrollPosition = 0

    init {
        CofolderDatabase.getDatabase(context)?.folderDao()?.let { dao ->
            repo = ImagesRepository(dao)
        }
    }

    fun showAsGird(): Boolean {
        return repo?.let { repo ->
            repo.showAsGrid()
        } ?: run { false }
    }

    fun setShowAsGrid() {
        repo?.changeGridOrList()
        openRvWithAnimation = true
        foldersSorted.value?.let { view?.setRecyclerView(it) }
    }

    fun onShortClick(folder: FolderFile?) {
        if (!canClick) return
        ifLet(repo, folder) { (repo, folder) ->
            view?.navigateEditPage((repo as ImagesRepository).createBundle(folder as FolderFile))
        }
    }

    fun onLongClick(folder: FolderFile?): Boolean {
        return if (!canClick || repo == null || folder == null) false
        else {
            repo?.let { repo ->
                view?.showBottomSheet(repo.createBundle(folder))
            }
            true
        }
    }

    fun addFolderClicked() {
        if (!canClick) return
        repo?.let { repo ->
            val folderId = repo.createUniqueId()
            repo.createNewFolder(folderId, "")?.let { folder ->
                viewModelScope.launch {
                    withContext(Dispatchers.IO) {
                        repo.save(folderId, folder)
                    }
                }
                view?.navigateEditPage(repo.createBundle(folder))
            }
        }
    }

    fun onItemRangeChanged(startPos: Int, endPos: Int) {
        PreferenceManager.setSorting(FOLDER, BY_DEFAULT)
        Collections.swap(foldersSorted.value as ArrayList<FolderFile>, startPos, endPos)
        val result = ArrayList<FolderFile>()
        result.addAll(foldersSorted.value as ArrayList<FolderFile>)
        if (PreferenceManager.showOnlyPrivate(FOLDER)) {
            var publicFolders = ArrayList<FolderFile>()
            publicFolders.addAll(allFolders.value as ArrayList<FolderFile>)
            publicFolders =
                publicFolders.filter { it.contributors.size > 1 } as ArrayList<FolderFile>
            result.addAll(publicFolders)
        }
        foldersSorted.value?.let {
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    repo?.updateRange(result)
                }
            }
        }
    }

    fun confirmed() {
        ifLet(repo, folderToDeleteOrLeave.value) { (repo, folderToDeleteOrLeave) ->
            view?.removeFromList((folderToDeleteOrLeave as FolderFile).id)
            viewModelScope.launch {
                if ((repo as ImagesRepository).isCreator(folderToDeleteOrLeave)) {
                    if ((folderToDeleteOrLeave as FolderFile).offline) {
                        dao?.deleteById(folderToDeleteOrLeave.id)
                    } else withContext(Dispatchers.IO) {
                        repo.delete(folderToDeleteOrLeave)
                    }
                } else withContext(Dispatchers.IO) {
                    repo.leave(folderToDeleteOrLeave as FolderFile)
                }
            }
        }
    }

    fun isCreator(position: Int): Boolean? {
        return try {
            repo?.isCreator(foldersSorted.value?.get(position))
        } catch (e: Exception) {
            null
        }
    }

    fun onSwiped(position: Int) {
        folderToDeleteOrLeave.value = foldersSorted.value?.get(position)
        repo?.let { repo ->
            if (repo.isCreator(folderToDeleteOrLeave.value)) {
                view?.showConfirmationDialog(
                    context.getString(R.string.confirm_delete_start) + " \"" + folderToDeleteOrLeave.value?.name + "\"? " + context.getString(
                        R.string.confirm_delete_end
                    )
                )
            } else {
                view?.showConfirmationDialog(
                    context.getString(R.string.confirm_leave_start) + " \"" + folderToDeleteOrLeave.value?.name + "\"? " + context.getString(
                        R.string.confirm_leave_end
                    )
                )
            }
        }
    }

    fun sort() {
        allFolders.value?.let { allFolders ->
            foldersSorted.value = repo?.sort(allFolders)
        }
    }

    fun nameUpdated(updatedFolder: FolderFile?, name: String, timestamp: Timestamp) {
        updatedFolder?.name = name
        updatedFolder?.dateOfLastEdit = timestamp
        updatedFolder?.let { sync(updatedFolder) }
    }

    fun colorUpdated(updatedFolder: FolderFile?, color: Int, timestamp: Timestamp) {
        updatedFolder?.color = color
        updatedFolder?.dateOfLastEdit = timestamp
        updatedFolder?.let { sync(updatedFolder) }
    }

    private fun sync(updatedFolder: FolderFile) {
        allFolders.value?.let { allFolders ->
            val folders = ArrayList<FolderFile>()
            allFolders.forEach { folder ->
                if (folder.id == updatedFolder.id) {
                    folders.add(updatedFolder)
                } else {
                    folders.add(folder)
                }
            }
            this.allFolders.value = folders
            sort()
        }
    }
}