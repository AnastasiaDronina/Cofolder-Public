package com.dronina.cofolder.data.repository

import com.dronina.cofolder.data.firebase.FirebaseSource
import com.dronina.cofolder.data.model.entities.BaseFile
import com.dronina.cofolder.data.model.other.Contributor
import com.dronina.cofolder.data.model.entities.User
import com.google.firebase.firestore.ktx.toObject

class SharingRepository {
    var listener: UpdateListener? = null

    interface UpdateListener {
        val removingInProgress: Boolean
        fun updated(data: BaseFile)
    }

    fun addContributorsUpdateListener(listener: UpdateListener, data: BaseFile?, dataType: Int?) {
        this.listener = listener
        if (data == null || dataType == null) return
        try {
            FirebaseSource.firestoreRef(dataType, data.id)?.addSnapshotListener { snapshot, e ->
                if (snapshot == null || !snapshot.exists() || listener.removingInProgress) return@addSnapshotListener
                snapshot.toObject<BaseFile>()?.let { file -> listener.updated(file) }
            }
        } catch (e: Exception) {
        }
    }

    fun getContributors(
        creator: String,
        contributorsIds: List<String>,
        editors: List<String>,
        allUsers: List<User>
    ): ArrayList<Contributor> {
        val contributors = ArrayList<Contributor>()
        contributorsIds.forEach { id ->
            (allUsers as ArrayList<User>).find { user ->
                user.id == id
            }?.let { contributor ->
                contributors.add(
                    Contributor(
                        user = contributor,
                        isMe = id == FirebaseSource.userId(),
                        isCreator = id == creator,
                        isEditor = editors.contains(id)
                    )
                )
            }
        }
        return contributors
    }

    suspend fun addContributor(dataType: Int?, user: String?, id: String?) {
        if (dataType == null || user == null || id == null) return
        try {
            FirebaseSource.addContributor(user, dataType, id)
            FirebaseSource.addToArray(user, dataType, id)
        } catch (e: Exception) {
        }
    }

    suspend fun removeContributor(dataType: Int?, id: String?, user: User?) {
        if (dataType == null || id == null || user == null) return
        try {
            FirebaseSource.removeContributor(user.id, dataType, id)
            FirebaseSource.removeFromArray(user.id, dataType, id)
        } catch (e: Exception) {
        }
    }

    suspend fun editorUpdated(dataType: Int?, id: String?, contributor: Contributor?) {
        if (dataType == null || id == null || contributor == null) return
        try {
            if (contributor.isEditor) {
                FirebaseSource.addToEditors(dataType, id, contributor.id)
            } else {
                FirebaseSource.removeFromEditors(dataType, id, contributor.id)
            }
        } catch (e: Exception) {
        }
    }

    fun amIEditor(data: BaseFile): Boolean {
        return data.editors.contains(FirebaseSource.userId())
                || data.creator == FirebaseSource.userId()
    }

    /*
     * Gets list of value pairs User to Boolean (boolean value representing if user a contributor or not),
     * used for AddContributorsViewModel to show available friends to add as contributors
     */
    fun getContributorsToAdd(
        contributorsIds: List<String>,
        friends: List<User>
    ): List<Pair<User, Boolean>>? {
        contributorsIds.forEach { id ->
            (friends as ArrayList<User>).removeAll { friend ->
                friend.id == id
            }
        }
        val contributorsToAdd = ArrayList<Pair<User, Boolean>>()
        friends.forEach { friend ->
            contributorsToAdd.add(Pair(friend, false))
        }
        return contributorsToAdd
    }
}
