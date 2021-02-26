package com.dronina.cofolder.ui.bottomsheet

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dronina.cofolder.R
import com.dronina.cofolder.data.model.entities.BaseFile
import com.dronina.cofolder.data.repository.BaseRepository
import com.dronina.cofolder.data.room.CofolderDatabase
import com.dronina.cofolder.data.room.dao.FolderDao
import com.dronina.cofolder.data.room.dao.ListDao
import com.dronina.cofolder.data.room.dao.NoteDao
import com.dronina.cofolder.utils.extensions.connected
import com.dronina.cofolder.utils.extensions.ifLet
import com.dronina.cofolder.utils.other.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BottomSheetViewModel(private val context: Context) : ViewModel() {
    private var baseRepo: BaseRepository? = null
    private var noteDao: NoteDao? = null
    private var listDao: ListDao? = null
    private var folderDao: FolderDao? = null

    var view: BottomSheetFragment? = null
    var isCreator = false
    var data: BaseFile? = null
    var dataType: Int? = null

    init {
        baseRepo = BaseRepository()
        noteDao = CofolderDatabase.getDatabase(context)?.noteDao()
        listDao = CofolderDatabase.getDatabase(context)?.listDao()
        folderDao = CofolderDatabase.getDatabase(context)?.folderDao()
    }

    fun onViewCreated(bundle: Bundle?) {
        ifLet(bundle, baseRepo) { (bundle, baseRepo) ->
            data = (bundle as Bundle).getParcelable(DATA_BUNDLE)
            dataType = bundle.getInt(DATA_TYPE)
            isCreator = (baseRepo as BaseRepository).isCreator(data)
        }
    }

    fun shareOnClick() {
        if (connected()) {
            view?.navigateSharePage()
        } else {
            view?.networkError()
        }
    }

    fun confirmed() {
        ifLet(baseRepo, data, dataType) { (baseRepo, data, dataType) ->
            viewModelScope.launch {
                if ((baseRepo as BaseRepository).isCreator(data)) {
                    if ((data as BaseFile).offline) {
                        view?.removeFromList()
                        when (dataType) {
                            NOTE -> noteDao?.deleteById(data.id)
                            LIST -> listDao?.deleteById(data.id)
                            FOLDER -> folderDao?.deleteById(data.id)
                        }
                        view?.refreshList()
                    } else {
                        view?.removeFromList()
                        withContext(Dispatchers.IO) {
                            baseRepo.delete(dataType as Int, data)
                        }
                    }
                } else {
                    view?.removeFromList()
                    withContext(Dispatchers.IO) {
                        baseRepo.leave(dataType as Int, data)
                    }
                }
            }
        }
    }

    fun leaveOrDelete() {
        if (isCreator) {
            view?.showConfirmationDialog(
                context.getString(R.string.confirm_delete_start) + " \"" + data?.name + "\"? " + context.getString(
                    R.string.confirm_delete_end
                )
            )
        } else {
            view?.showConfirmationDialog(
                context.getString(R.string.confirm_leave_start) + " \"" + data?.name + "\"? " + context.getString(
                    R.string.confirm_leave_end
                )
            )
        }
    }
}