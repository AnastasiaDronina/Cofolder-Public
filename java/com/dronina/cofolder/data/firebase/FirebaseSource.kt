package com.dronina.cofolder.data.firebase

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import com.dronina.cofolder.CofolderApp
import com.dronina.cofolder.R
import com.dronina.cofolder.data.model.entities.Request
import com.dronina.cofolder.data.model.entities.User
import com.dronina.cofolder.data.preferences.PreferenceManager
import com.dronina.cofolder.utils.extensions.*
import com.dronina.cofolder.utils.other.*
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream

object FirebaseSource {
    fun subscribeToNotifications() {
        try {
            FirebaseMessaging.getInstance().subscribeToTopic("/topics/${userId()}")
        } catch (e: Exception) {
        }
    }

    fun unsubscribeFromNotifications() {
        try {
            FirebaseMessaging.getInstance().unsubscribeFromTopic("/topics/${userId()}")
        } catch (e: Exception) {

        }
    }

    fun signOut() {
        try {
            auth()?.signOut()
            unsubscribeFromNotifications()
        } catch (e: Exception) {

        }
    }

    fun currentUser(): FirebaseUser? {
        return try {
            auth()?.currentUser?.let { currentUser ->
                PreferenceManager.setCurrentUserIdOffline(currentUser.uid)
                currentUser
            } ?: run {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    fun userId(): String? {
        return try {
            currentUser()?.uid ?: PreferenceManager.currentUserIdOffline()
        } catch (e: Exception) {
            PreferenceManager.currentUserIdOffline()
        }
    }

    fun signInIntent(context: Context): Intent? {
        return try {
            val theme = when (context.whichTheme()) {
                LIGHT_THEME -> R.style.LoginTheme
                DARK_THEME -> R.style.LoginThemeDark
                else -> R.style.LoginTheme
            }
            val providers: List<AuthUI.IdpConfig> =
                arrayListOf(AuthUI.IdpConfig.PhoneBuilder().build())
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setTheme(theme)
                .setLogo(R.mipmap.ic_launcher)
                .build()
        } catch (e: Exception) {
            null
        }
    }

    fun firestoreRef(dataType: Int, id: String): DocumentReference? {
        return try {
            var collection = ""
            when (dataType) {
                USER -> collection = USERS_COLLECTION
                NOTE -> collection = NOTES_COLLECTION
                LIST -> collection = LISTS_COLLECTION
                FOLDER -> collection = FOLDERS_COLLECTION
                ISSUE -> collection = ISSUES_COLLECTION
            }
            firestore()?.collection(collection)?.document(id)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun addToArray(user: String, dataType: Int, valueToAdd: String): Boolean {
        return try {
            firestoreRef(USER, user)?.update(dataType.array(), FieldValue.arrayUnion(valueToAdd))
                ?.await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun removeFromArray(user: String, dataType: Int, valueToRemove: String): Boolean {
        return try {
            firestoreRef(USER, user)?.update(
                dataType.array(), FieldValue.arrayRemove(valueToRemove)
            )?.await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun addContributor(contributor: String, dataType: Int, id: String): Boolean {
        return try {
            firestoreRef(dataType, id)?.update(
                CONTRIBUTORS_ARRAY, FieldValue.arrayUnion(contributor)
            ).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun removeContributor(contributor: String, dataType: Int, id: String): Boolean {
        return try {
            firestoreRef(dataType, id)?.update(
                CONTRIBUTORS_ARRAY, FieldValue.arrayRemove(contributor)
            )
            firestoreRef(dataType, id)?.update(EDITORS_ARRAY, FieldValue.arrayRemove(contributor))
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun addToEditors(dataType: Int, id: String, userId: String): Boolean {
        return try {
            firestoreRef(dataType, id)?.update(
                EDITORS_ARRAY, FieldValue.arrayUnion(userId)
            ).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun removeFromEditors(dataType: Int, id: String, userId: String): Boolean {
        return try {
            firestoreRef(dataType, id)?.update(
                EDITORS_ARRAY, FieldValue.arrayRemove(userId)
            ).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun userDatabaseRef(): DocumentReference? {
        return try {
            currentUser()?.let { currentUser ->
                firestoreRef(USER, currentUser.uid)
            } ?: run {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun currentUserObject(): User? {
        return try {
            userDatabaseRef()?.get()?.await()?.toObject<User>()
        } catch (e: Exception) {
            null
        }
    }

    fun collection(dataType: Int): CollectionReference? {
        return try {
            firestore()?.collection(dataType.collection())
        } catch (e: Exception) {
            null
        }
    }

    fun imagesInsideFolder(folderId: String): CollectionReference? {
        return try {
            firestoreRef(FOLDER, folderId)?.collection(IMAGES_COLLECTION)
        } catch (e: Exception) {
            null
        }
    }

    fun imageDatabaseRef(imageId: String, folderId: String): DocumentReference? {
        return try {
            imagesInsideFolder(folderId)?.document(imageId)
        } catch (e: Exception) {
            null
        }
    }

    fun imageStorageRef(imageId: String, folderId: String): StorageReference? {
        return try {
            cloudStorage()?.child(folderId)?.child(imageId)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun loadProfileImage(imageUri: Uri, id: String): String? {
        return try {
            CofolderApp.context?.let { context ->
                val ref = cloudStorage()?.child(PROFILE_IMAGES)?.child(id)
                val rotatedImage = imageUri.rotate(context.contentResolver)
                val baos = ByteArrayOutputStream()
                rotatedImage?.compress(Bitmap.CompressFormat.JPEG, 50, baos)
                val data = baos.toByteArray()
                val url = ref?.putBytes(data)?.await()?.storage?.downloadUrl?.await()
                url?.toString() ?: ""
            }
        } catch (e: Exception) {
            ""
        }
    }

    fun deleteProfileImage(id: String) {
        cloudStorage()?.child(PROFILE_IMAGES)?.child(id)?.delete()
    }

    suspend fun sendRequest(id: String, receiver: User?): Boolean {
        val sender = currentUserObject()
        return if (sender == null || receiver == null) false
        else {
            val req = Request(id, sender, receiver, currentTime())
            try {
                firestore()?.collection(REQUESTS_COLLECTION)?.document(id)?.set(req)?.await()
                true
            } catch (e: Exception) {
                false
            }
        }
    }

    suspend fun cancelRequest(receiver: User?): Boolean {
        val sender = currentUserObject()
        val request = getRequest(sender, receiver)
        return if (sender == null || receiver == null || request == null) false
        else {
            try {
                requestRef(request)?.delete()?.await()
                true
            } catch (e: Exception) {
                false
            }
        }
    }

    suspend fun getRequests(): List<Request>? {
        return try {
            firestore()?.collection(REQUESTS_COLLECTION)?.get()?.await()?.toObjects()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun acceptRequest(sender: User?): Boolean {
        val receiver = currentUserObject()
        val request = getRequest(sender, receiver)

        return if (sender == null || receiver == null || request == null) false
        else {
            val removed = try {
                requestRef(request)?.delete()?.await()
                true
            } catch (e: Exception) {
                false
            }
            removed && addToArray(sender.id, USER, receiver.id)
                    && addToArray(receiver.id, USER, sender.id)
        }
    }

    suspend fun removeFriend(friend: User?): Boolean {
        val me = currentUserObject()
        return if (me == null || friend == null) false
        else {
            removeFromArray(user = me.id, dataType = USER, valueToRemove = friend.id)
                    && removeFromArray(user = friend.id, dataType = USER, valueToRemove = me.id)
        }
    }

    private fun requestRef(request: Request): DocumentReference? {
        return try {
            firestore()?.collection(REQUESTS_COLLECTION)?.document(request.id)
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun getRequest(sender: User?, receiver: User?): Request? {
        val requests = getRequests()
        return if (sender == null || receiver == null || requests == null) null
        else {
            requests.find { request ->
                request.sender?.id == sender.id && request.receiver?.id == receiver.id
            }
        }
    }

    private fun auth(): FirebaseAuth? {
        return try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            null
        }
    }

    private fun firestore(): FirebaseFirestore? {
        return try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            null
        }
    }

    private fun cloudStorage(): StorageReference? {
        return try {
            FirebaseStorage.getInstance().reference
        } catch (e: Exception) {
            null
        }
    }
}