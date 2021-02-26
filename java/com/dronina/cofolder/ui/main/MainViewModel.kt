package com.dronina.cofolder.ui.main

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dronina.cofolder.R
import com.dronina.cofolder.data.model.entities.*
import com.dronina.cofolder.data.repository.ImagesRepository
import com.dronina.cofolder.data.repository.ListsRepository
import com.dronina.cofolder.data.repository.NotesRepository
import com.dronina.cofolder.data.repository.UserRepository
import com.dronina.cofolder.data.room.CofolderDatabase
import com.dronina.cofolder.utils.extensions.toast
import com.dronina.cofolder.utils.other.FOLDER
import com.dronina.cofolder.utils.other.LIST
import com.dronina.cofolder.utils.other.NOTE
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(private val context: Context) : ViewModel(), UserRepository.UserUpdateListener,
    UserRepository.RequestsUpdateListener, ImagesRepository.AllFoldersUpdateListener {
    private var userRepo: UserRepository? = null
    private var notesRepo: NotesRepository? = null
    private var listsRepo: ListsRepository? = null
    private var imagesRepo: ImagesRepository? = null
    val currentRequests = MutableLiveData<Pair<List<Request>, List<Request>>>()
    val currentUsers = MutableLiveData<List<User>>()
    val currentFriends = MutableLiveData<List<User>>()
    val currentNotes = MutableLiveData<List<NoteFile>>()
    val currentLists = MutableLiveData<List<ListFile>>()
    val currentFolders = MutableLiveData<List<FolderFile>>()
    val currentUser = MutableLiveData<User>()
    val imagesSpace = MutableLiveData<Int>()
    private val allFolders = MutableLiveData<List<FolderFile>>()

    init {
        CofolderDatabase.getDatabase(context)?.let { database ->
            userRepo = UserRepository(database.userDao())
            notesRepo = NotesRepository(database.noteDao())
            listsRepo = ListsRepository(database.listDao())
            imagesRepo = ImagesRepository(database.folderDao())
        }
    }

    fun onCreate() {
        userRepo?.addRequestsUpdateListener(this)
        userRepo?.addUserUpdateListener(this)
        userRepo?.addAllUsersUpdateListener(this)
        imagesRepo?.addAllFoldersUpdateListener(this)
        userRepo?.subscribeOrUnsubscribeNotifications()
    }

    fun findUser(id: String): User? {
        return currentUsers.value?.find { user ->
            user.id == id
        }
    }

    override fun requestsUpdated(requests: Pair<List<Request>, List<Request>>) {
        this.currentRequests.value = requests
    }

    fun removeItem(dataType: Int, id: String) {
        when (dataType) {
            NOTE -> if (currentNotes.value != null) {
                val notes = ArrayList<NoteFile>()
                currentNotes.value?.let { notes.addAll(it) }
                notes.removeAll { it.id == id }
                currentNotes.value = notes
            }
            LIST -> if (currentLists.value != null) {
                val lists = ArrayList<ListFile>()
                currentLists.value?.let { lists.addAll(it) }
                lists.removeAll { it.id == id }
                currentLists.value = lists
            }
            FOLDER -> if (currentFolders.value != null) {
                val folders = ArrayList<FolderFile>()
                currentFolders.value?.let { folders.addAll(it) }
                folders.removeAll { it.id == id }
                currentFolders.value = folders
            }
        }
    }

    override fun userUpdated(user: User?) {
        user?.let {
            currentUser.value = user
            viewModelScope.launch {
                user.friends?.let { userFriends ->
                    var friends: ArrayList<User>? = null
                    withContext(IO) {
                        friends = userRepo?.getFriends(userFriends)
                    }
                    friends?.let {
                        this@MainViewModel.currentFriends.value = friends
                    }
                }
                user.notes?.let { userNotes ->
                    var notes: ArrayList<NoteFile>? = null
                    withContext(IO) {
                        notes = notesRepo?.getNotes(userNotes)
                    }
                    notes?.let {
                        this@MainViewModel.currentNotes.value = notes
                    }
                }
                user.lists?.let { userLists ->
                    var lists: ArrayList<ListFile>? = null
                    withContext(IO) {
                        lists = listsRepo?.getLists(userLists)
                    }
                    lists?.let {
                        this@MainViewModel.currentLists.value = lists
                    }
                }
                user.folders?.let { userFolders ->
                    var folders: ArrayList<FolderFile>? = null
                    withContext(IO) {
                        folders = imagesRepo?.getFolders(userFolders)
                    }
                    folders?.let {
                        this@MainViewModel.currentFolders.value = folders
                    }
                }
            }
        }
    }

    override fun allUsersUpdated(allUsers: List<User>) {
        this.currentUsers.value = allUsers
    }

    override fun foldersUpdated(folders: List<FolderFile>) {
        allFolders.value = folders
        viewModelScope.launch {
            var space = 0
            withContext(IO) {
                if (currentUser.value == null || imagesRepo == null || allFolders.value == null) return@withContext
                allFolders.value?.let { allFolders ->
                    allFolders.forEach { folder ->
                        imagesRepo?.getImages(folder.id)?.let { images ->
                            val filtered = images.filter { it.creator == currentUser.value?.id }
                            space += filtered.size
                        }
                    }
                }
            }
            imagesSpace.value = space
        }
    }

    fun refreshNotes() {
        viewModelScope.launch {
            var notes: ArrayList<NoteFile>? = null
            withContext(IO) {
                val user = userRepo?.getCurrentUserFromDb()
                user?.notes?.let { userNotes ->
                    notes = notesRepo?.getNotes(userNotes)
                }
            }
            notes?.let {
                this@MainViewModel.currentNotes.value = notes
            }
        }
    }

    fun refreshLists() {
        viewModelScope.launch {
            var lists: ArrayList<ListFile>? = null
            withContext(IO) {
                val user = userRepo?.getCurrentUserFromDb()
                user?.lists?.let { userLists ->
                    lists = listsRepo?.getLists(userLists)
                }
            }
            lists?.let {
                this@MainViewModel.currentLists.value = lists
            }
        }
    }

    fun networkError() {
        context.toast(context.getString(R.string.network_error))
    }
}