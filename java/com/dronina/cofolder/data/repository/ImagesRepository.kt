package com.dronina.cofolder.data.repository

import android.net.Uri
import android.os.Bundle
import com.dronina.cofolder.CofolderApp
import com.dronina.cofolder.data.firebase.FirebaseSource
import com.dronina.cofolder.data.model.entities.FolderFile
import com.dronina.cofolder.data.model.entities.Image
import com.dronina.cofolder.data.room.dao.FolderDao
import com.dronina.cofolder.data.services.LoadImagesService
import com.dronina.cofolder.utils.extensions.connected
import com.dronina.cofolder.utils.extensions.currentTime
import com.dronina.cofolder.utils.other.*
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import kotlinx.coroutines.tasks.await

class ImagesRepository(private val dao: FolderDao) : BaseRepository() {
    private var folderUpdateListener: FolderUpdateListener? = null
    private var allFoldersUpdateListener: AllFoldersUpdateListener? = null

    interface FolderUpdateListener {
        fun folderUpdated(folder: FolderFile)
        fun folderUpdatedByMe(folder: FolderFile)
    }

    interface AllFoldersUpdateListener {
        fun foldersUpdated(folders: List<FolderFile>)
    }

    fun addFolderUpdateListener(listener: FolderUpdateListener, id: String) {
        folderUpdateListener = listener
        try {
            FirebaseSource.firestoreRef(FOLDER, id)?.addSnapshotListener { snapshot, e ->
                if (snapshot == null || !snapshot.exists()) return@addSnapshotListener
                val updatedFolder = snapshot.toObject<FolderFile>()
                if (updatedFolder == null || updatedFolder.offline) return@addSnapshotListener
                if (updatedFolder.lastUserEdited == FirebaseSource.userId()) {
                    folderUpdateListener?.folderUpdatedByMe(updatedFolder)
                } else {
                    folderUpdateListener?.folderUpdated(updatedFolder)
                }
            }
        } catch (e: Exception) {
        }
    }

    fun addAllFoldersUpdateListener(listener: AllFoldersUpdateListener) {
        allFoldersUpdateListener = listener
        try {
            FirebaseSource.collection(FOLDER)?.addSnapshotListener { snapshot, e ->
                snapshot?.toObjects<FolderFile>()?.let { folders ->
                    allFoldersUpdateListener?.foldersUpdated(folders)
                }
            }
        } catch (e: Exception) {
        }
    }

    suspend fun updateName(folder: FolderFile, name: String) {
        if (folder.offline) {
            dao.updateName(folder.id, name)
        } else {
            if (connected()) {
                folder.name = name
                save(folder.id, folder)
            } else {
                FirebaseSource.firestoreRef(FOLDER, folder.id)?.update(NAME_FIELD, name).await()
            }
        }
    }

    suspend fun updateColor(folder: FolderFile, color: Int) {
        if (folder.offline) {
            dao.updateColor(folder.id, color)
        } else {
            if (connected()) {
                folder.color = color
                save(folder.id, folder)
            } else {
                FirebaseSource.firestoreRef(FOLDER, folder.id)?.update(COLOR_FIELD, color)
                    .await()
            }
        }
    }

    suspend fun save(id: String, folder: FolderFile): Boolean {
        return if (super.save(FOLDER, id, folder)) {
            true
        } else {
            folder.offline = true
            try {
                dao.insert(folder)
                true
            } catch (e: Exception) {
                false
            }
        }
    }

    suspend fun delete(folder: FolderFile): Boolean {
        deleteFolderFromStorage(folder)
        return if (super.delete(FOLDER, folder)) {
            true
        } else try {
            dao.delete(folder)
            true
        } catch (e: Exception) {
            false
        }
    }


    suspend fun deleteImage(folderId: String?, imageId: String): Boolean {
        return folderId?.let {
            try {
                FirebaseSource.imageDatabaseRef(imageId, folderId)?.delete()
                    ?.await()
                FirebaseSource.imageStorageRef(imageId, folderId)?.delete()
                    ?.await()
                true
            } catch (e: Exception) {
                false
            }
        } ?: run {
            false
        }
    }

    suspend fun leave(folder: FolderFile): Boolean {
        return super.leave(FOLDER, folder)
    }

    fun sort(list: ArrayList<FolderFile>): ArrayList<FolderFile> {
        return super.sort(FOLDER, list)
    }

    fun showAsGrid(): Boolean {
        return super.showAsGrid(FOLDER)
    }

    fun changeGridOrList() {
        return super.changeGridOrList(FOLDER)
    }

    override fun createUniqueId(): String {
        return "folder_" + super.createUniqueId()
    }

    suspend fun getFolders(ids: List<String>): ArrayList<FolderFile>? {
        return super.getListOf<FolderFile>(FOLDER, ids).addOfflineFolders()
    }

    fun createBundle(folder: FolderFile): Bundle {
        val bundle = super.createBundle(FOLDER, folder)
        bundle.putParcelable(FOLDER_BUNDLE, folder)
        return bundle
    }

    suspend fun updateRange(folders: ArrayList<FolderFile>) {
        val foldersIds = ArrayList<String>()
        folders.forEach { folder ->
            foldersIds.add(folder.id)
        }
        FirebaseSource.currentUserObject()?.let { user ->
            user.folders = foldersIds
            FirebaseSource.firestoreRef(USER, user.id)?.set(user)?.await()
        }
    }

    fun createNewFolder(id: String, name: String): FolderFile? {
        return FirebaseSource.userId()?.let { userId ->
            FolderFile(
                id = id,
                name = name,
                creator = userId,
                dateOfLastEdit = currentTime(),
                contributors = listOf(userId),
                color = NO_COLOR,
                offline = false,
                images = emptyList(),
                lastUserEdited = userId
            )
        }
    }

    private fun deleteImageSync(folderId: String, imageId: String) {
        try {
            FirebaseSource.imageStorageRef(imageId, folderId)?.delete()
        } catch (e: Exception) {
        }
    }

    private fun deleteFolderFromStorage(folder: FolderFile) {
        try {
            folder.images.forEach { imageId ->
                deleteImageSync(folder.id, imageId)
            }
        } catch (e: Exception) {
        }
    }

    private suspend fun ArrayList<FolderFile>.addOfflineFolders(): ArrayList<FolderFile> {
        val offlineFolders = dao.getAll()
        return if (offlineFolders.isEmpty()) this
        else {
            if (!connected()) {
                this.addAll(offlineFolders)
            } else {
                offlineFolders.forEach { folder ->
                    folder.offline = false
                    val savedOnServerSuccessfully = save(folder.id, folder)
                    if (savedOnServerSuccessfully) {
                        folder.offline = true
                        dao.delete(folder)
                    }
                    this.add(folder)
                }
            }
            this
        }
    }

    fun isImageCreator(image: Image): Boolean {
        return image.creator == FirebaseSource.userId()
    }

    suspend fun getImages(folderId: String?): List<Image>? {
        return folderId?.let {
            FirebaseSource.imagesInsideFolder(folderId)?.get()?.await()?.toObjects()
        }
    }

    fun uploadImagesInBackground(folderId: String, images: ArrayList<Uri>, currentImagesSpace: Int) {
        CofolderApp.context?.let { context ->
            LoadImagesService.startService(context, folderId, images, currentImagesSpace)
        }
    }

    fun createUniqueImageId(): String {
        return "${FirebaseSource.userId()}_${System.currentTimeMillis()}"
    }

    fun createBlankImage(imageId: String, downloadUrl: String): Image? {
        return FirebaseSource.userId()?.let { userId ->
            Image(
                id = imageId,
                url = downloadUrl,
                creator = userId,
                dateOfCreation = currentTime()
            )
        }
    }
}

