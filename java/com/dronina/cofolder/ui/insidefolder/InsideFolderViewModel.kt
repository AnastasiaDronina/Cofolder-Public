package com.dronina.cofolder.ui.insidefolder

import android.R
import android.app.Activity
import android.content.*
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.provider.MediaStore
import androidx.lifecycle.*
import com.dronina.cofolder.data.model.entities.FolderFile
import com.dronina.cofolder.data.model.entities.Image
import com.dronina.cofolder.data.repository.ImagesRepository
import com.dronina.cofolder.data.room.CofolderDatabase
import com.dronina.cofolder.data.services.LoadImagesService
import com.dronina.cofolder.utils.extensions.connected
import com.dronina.cofolder.utils.extensions.currentTime
import com.dronina.cofolder.utils.extensions.ifLet
import com.dronina.cofolder.utils.other.DATA_BUNDLE
import com.dronina.cofolder.utils.other.RC_PICK_IMAGES
import com.google.firebase.Timestamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class InsideFolderViewModel(private val context: Context) : ViewModel(),
    ImagesRepository.FolderUpdateListener {
    private var repo: ImagesRepository? = null
    var view: InsideFolderFragment? = null
    var lifecycleOwner: LifecycleOwner? = null
    val navigateWithoutTransition = MutableLiveData<Boolean>()
    var userIsEditor = MutableLiveData<Boolean>()
    var currentFolderId: String? = null
    var currentFolder = MutableLiveData<FolderFile>()
    var images = MutableLiveData<ArrayList<Image>>()
    var scrollPosition = MutableLiveData<Int>()
    val canAddImages = MutableLiveData<Boolean>()
    var canClick = true

    init {
        canAddImages.value = true
        CofolderDatabase.getDatabase(context)?.folderDao()?.let { dao ->
            repo = ImagesRepository(dao)
        }
    }

    fun onViewCreated(arguments: Bundle?) {
        val folderFromBundle = arguments?.getParcelable<FolderFile>(DATA_BUNDLE)
        if (currentFolder.value == null) {
            currentFolder.value = folderFromBundle
        } else if (currentFolder.value?.id != folderFromBundle?.id) {
            currentFolder.value = folderFromBundle
        }
        userIsEditor.value = repo?.isEditor(currentFolder.value)
        currentFolderId = currentFolder.value?.let { currentFolder ->
            view?.setName(currentFolder.name)
            view?.setColor(currentFolder.color)
            syncContents()
            currentFolder.id
        } ?: run {
            repo?.createUniqueId()
        }
        currentFolderId?.let { id ->
            repo?.addFolderUpdateListener(this, id)
        }
    }

    fun syncContents() {
        viewModelScope.launch {
            var imagesSynced: ArrayList<Image>? = null
            withContext(Dispatchers.IO) {
                imagesSynced = repo?.getImages(currentFolderId) as ArrayList<Image>
            }
            imagesSynced?.let { imagesSynced ->
                images.value = imagesSynced
            } ?: run {
                images.value = ArrayList()
            }
        }
    }

    fun shareOnClick() {
        ifLet(repo, currentFolder.value) { (repo, currentFolder) ->
            if (connected()) {
                view?.navigateSharePage((repo as ImagesRepository).createBundle(currentFolder as FolderFile))
            } else {
                view?.networkError()
            }
        }
    }

    fun showAsCreator(): Boolean {
        return repo?.let { repo ->
            currentFolder.value?.let { currentFolder ->
                repo.isCreator(currentFolder)
            } ?: run { false }
        } ?: run { false }
    }

    fun time(): Timestamp {
        return currentTime()
    }

    fun nameUpdated(newName: String) {
        ifLet(currentFolder.value, userIsEditor.value) { (folder, isEditor) ->
            if (!(isEditor as Boolean) || (folder as FolderFile).name == newName) return
            folder.name = newName
            currentFolder.value = folder
            viewModelScope.launch {
                withContext(Dispatchers.IO) { repo?.updateName(folder, newName) }
            }
        }
    }

    fun colorUpdated(color: Int) {
        ifLet(currentFolder.value, userIsEditor.value) { (folder, isEditor) ->
            if (!(isEditor as Boolean) || (folder as FolderFile).color == color) return
            view?.removeBoarder(folder.color)
            view?.drawBoarder(color)
            folder.color = color
            currentFolder.value = folder
            viewModelScope.launch {
                withContext(Dispatchers.IO) { repo?.updateColor(folder, color) }
            }
        }
    }

    fun confirmed() {
        ifLet(repo, currentFolder.value) { (repo, currentFolder) ->
            view?.removeFolderFromList()
            viewModelScope.launch {
                if (showAsCreator()) withContext(Dispatchers.IO) {
                    (repo as ImagesRepository).delete(currentFolder as FolderFile)
                } else withContext(Dispatchers.IO) {
                    (repo as ImagesRepository).leave(currentFolder as FolderFile)
                }
            }
        }
    }

    fun deleteOrLeave() {
        if (showAsCreator()) {
            view?.showConfirmationDialog(
                context.getString(com.dronina.cofolder.R.string.confirm_delete_start) + " \"" + currentFolder.value?.name + "\"? " + context.getString(
                    com.dronina.cofolder.R.string.confirm_delete_end
                )
            )
        } else {
            view?.showConfirmationDialog(
                context.getString(com.dronina.cofolder.R.string.confirm_leave_start) + " \"" + currentFolder.value?.name + "\"? " + context.getString(
                    com.dronina.cofolder.R.string.confirm_leave_end
                )
            )
        }
    }

    override fun folderUpdated(folder: FolderFile) {
        syncContents(folder)
        syncName(folder)
        syncColor(folder)
        syncContributors(folder)
        view?.updateMainModel()
        syncContents()
    }

    override fun folderUpdatedByMe(folder: FolderFile) {
        syncContents(folder)
        syncContributors(folder)
        syncEditors(folder)
        view?.updateMainModel()
    }

    fun deleteImage(image: Image, position: Int) {
        imageDeleted(image)
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repo?.deleteImage(currentFolder.value?.id, image.id)
            }
        }
    }

    fun onActivityResult(
        contentResolver: ContentResolver,
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
        currentImagesSpace: Int
    ) {
        var imagesList = ArrayList<Uri>()
        try {
            if (requestCode == RC_PICK_IMAGES && resultCode == Activity.RESULT_OK && null != R.attr.data) {
                imagesList = getImagesUris(contentResolver, data)
            }
            if (imagesList.isNotEmpty()) {
                currentFolderId?.let { id ->
                    repo?.uploadImagesInBackground(
                        id,
                        imagesList,
                        currentImagesSpace
                    )
                }
                view?.bindService()
            } else canAddImages.value = true
        } catch (e: Exception) {
            view?.serverError()
        }
    }

    private fun syncContents(folder: FolderFile) {
        currentFolder.value?.images?.let { currentImages ->
            if (currentImages == folder.images) return@syncContents
            currentFolder.value?.images = folder.images
            viewModelScope.launch {
                var updateImages: ArrayList<Image>? = null
                withContext(Dispatchers.IO) {
                    updateImages = repo?.getImages(currentFolderId) as ArrayList<Image>
                }
                updateImages?.let {
                    images.value = updateImages
                } ?: run {
                    images.value = ArrayList()
                }
            }
        }
    }

    private fun syncName(folder: FolderFile) {
        currentFolder.value?.name?.let { currentName ->
            if (currentName == folder.name) return@syncName
            currentFolder.value?.name = folder.name
            view?.setName(folder.name)
        }
    }

    private fun syncColor(folder: FolderFile) {
        currentFolder.value?.color?.let { currentColor ->
            if (currentColor == folder.color) return@syncColor
            view?.removeBoarder(currentColor)
            view?.drawBoarder(folder.color)
            view?.setColor(folder.color)
        }
    }

    private fun syncContributors(folder: FolderFile) {
        currentFolder.value?.contributors?.let { currentContributors ->
            currentFolder.value?.contributors = folder.contributors
        }
    }

    private fun syncEditors(folder: FolderFile) {
        currentFolder.value?.editors?.let { currentEditors ->
            currentFolder.value?.editors = folder.editors
        }
    }

    private var service: LoadImagesService? = null
    private var isBound: Boolean = false
    val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, iBinder: IBinder) {
            val binder = iBinder as LoadImagesService.MyBinder
            service = binder.service
            isBound = true

            service?.let { notNullService ->
                lifecycleOwner?.let { notNullLifecycleOwner ->
                    service?.imageLoaded?.observe(notNullLifecycleOwner, Observer { image ->
                        images.value?.add(image)
                    })
                    service?.needsUnbind?.observe(notNullLifecycleOwner, Observer { needsUnbind ->
                        if (needsUnbind) {
                            view?.unbindService()
                            canAddImages.value = true
                        }
                    })
                }
            }
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            isBound = false
        }
    }

    private fun getImagesUris(resolver: ContentResolver, data: Intent?): ArrayList<Uri> {
        val imagesList = ArrayList<Uri>()
        var uri: Uri? = null

        val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
        data?.data?.let { imageUri ->
            uri = imageUri
            val cursor = resolver.query(imageUri, filePathColumn, null, null, null)
            cursor?.moveToFirst()
            cursor?.close()
        } ?: run {
            data?.clipData?.let { clipData ->
                for (i in 0 until clipData.itemCount) {
                    val item = clipData.getItemAt(i)
                    imagesList.add(item.uri)
                    val cursor = resolver.query(item.uri, filePathColumn, null, null, null)
                    cursor?.moveToFirst()
                    cursor?.close()
                }
            }
        }
        uri?.let { uri ->
            if (imagesList.size < 1) {
                imagesList.add(uri)
            }
        }
        return imagesList
    }

    private fun imageDeleted(image: Image) {
        images.value?.let { currentImages ->
            val images = ArrayList<Image>()
            images.addAll(currentImages)
            images.remove(image)
            this.images.value?.clear()
            this.images.value?.addAll(images)
            view?.refresh()
        }
    }

}