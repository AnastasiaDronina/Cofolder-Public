package com.dronina.cofolder.data.repository

import android.os.Bundle
import com.dronina.cofolder.CofolderApp
import com.dronina.cofolder.data.firebase.FirebaseSource
import com.dronina.cofolder.data.model.entities.*
import com.dronina.cofolder.data.preferences.PreferenceManager
import com.dronina.cofolder.utils.extensions.currentTime
import com.dronina.cofolder.utils.other.*
import com.google.firebase.firestore.ktx.toObjects
import kotlinx.coroutines.tasks.await
import java.util.*
import kotlin.collections.ArrayList

open class BaseRepository {
    suspend fun <T> save(dataType: Int, id: String, data: T): Boolean {
        return CofolderApp.instance?.let { appInstance ->
            if (!appInstance.isConnectedToNetwork()) false
            else {
                (data as BaseFile).dateOfLastEdit = currentTime()
                FirebaseSource.userId()?.let { userId ->
                    (data as BaseFile).lastUserEdited = userId
                }
                when (dataType) {
                    NOTE -> FirebaseSource.firestoreRef(NOTE, id)?.set(data as NoteFile)?.await()
                    LIST -> FirebaseSource.firestoreRef(LIST, id)?.set(data as ListFile)?.await()
                    FOLDER -> FirebaseSource.firestoreRef(FOLDER, id)?.set(data as FolderFile)
                        ?.await()
                }
                FirebaseSource.userId()?.let { userId ->
                    FirebaseSource.addToArray(userId, dataType, id)
                }
                true
            }
        } ?: run {
            false
        }
    }

    suspend fun <T> delete(dataType: Int, data: T): Boolean {
        return if ((data as BaseFile).offline) false
        else try {
            val id = (data as BaseFile).id
            val contributors = (data as BaseFile).contributors
            when (dataType) {
                NOTE -> FirebaseSource.firestoreRef(NOTE, id)?.delete()
                LIST -> FirebaseSource.firestoreRef(LIST, id)?.delete()
                FOLDER -> FirebaseSource.firestoreRef(FOLDER, id)?.delete()
            }
            contributors.forEach { contributor ->
                FirebaseSource.removeFromArray(contributor, dataType, id)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun <T> leave(dataType: Int, data: T): Boolean {
        return if ((data as BaseFile).offline) false
        else try {
            val id = (data as BaseFile).id
            FirebaseSource.userId()?.let { userId ->
                FirebaseSource.removeContributor(userId, dataType, id)
                FirebaseSource.removeFromArray(userId, dataType, id)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun <T> getListOf(dataType: Int, ids: List<String>): ArrayList<T> {
        val list = ArrayList<T>()
        val collection = FirebaseSource.collection(dataType)
        val all: List<T>? = try {
            when (dataType) {
                USER -> collection?.get()?.await()?.toObjects<User>() as ArrayList<T>
                NOTE -> collection?.get()?.await()?.toObjects<NoteFile>() as ArrayList<T>
                LIST -> collection?.get()?.await()?.toObjects<ListFile>() as ArrayList<T>
                FOLDER -> collection?.get()?.await()?.toObjects<FolderFile>() as ArrayList<T>
                else -> null
            }
        } catch (e: Exception) {
            null
        }

        ids.forEach { id ->
            val item = all?.find {
                (it as BaseFile).id == id
            }
            if (item != null) {
                list.add(item)
            }
        }
        return list
    }

    fun <T> createBundle(dataType: Int, data: T): Bundle {
        val bundle = Bundle()
        bundle.putInt(DATA_TYPE, dataType)
        bundle.putParcelable(DATA_BUNDLE, data as BaseFile)
        return bundle
    }

    fun <T> isCreator(data: T): Boolean {
        return (data as BaseFile).creator == FirebaseSource.userId()
    }

    fun <T> isEditor(data: T): Boolean {
        return (data as BaseFile).editors.contains(FirebaseSource.userId()) || isCreator(data)
    }

    open fun createUniqueId(): String {
        return FirebaseSource.userId() + "_" + System.currentTimeMillis()
    }

    open fun showAsGrid(dataType: Int): Boolean {
        return PreferenceManager.showAsGrid(dataType)
    }

    fun changeGridOrList(dataType: Int) {
        PreferenceManager.setShowAsGrid(
            dataType,
            !PreferenceManager.showAsGrid(dataType)
        )
    }

    fun <T> sort(dataType: Int, list: ArrayList<T>): ArrayList<T> {
        when (PreferenceManager.sorting(dataType)) {
            BY_DATE -> list.sortByDescending {
                (it as BaseFile).dateOfLastEdit
            }
            BY_COLOR -> list.sortByDescending {
                (it as BaseFile).color
            }
            ALPHABETICALLY -> list.sortBy {
                (it as BaseFile).name.toLowerCase(Locale.ROOT)
            }
        }
        return if (PreferenceManager.showOnlyPrivate(dataType)) list.getOnlyPrivate()
        else list
    }

    private fun <T> List<T>.getOnlyPrivate(): ArrayList<T> {
        return filter {
            (it as BaseFile).contributors.size == 1
                    && (it as BaseFile).contributors[0] == FirebaseSource.userId()
        } as ArrayList<T>
    }
}