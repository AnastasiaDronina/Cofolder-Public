package com.dronina.cofolder.data.repository

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.dronina.cofolder.R
import com.dronina.cofolder.data.firebase.FirebaseSource
import com.dronina.cofolder.data.model.other.Contact
import com.dronina.cofolder.data.model.entities.Request
import com.dronina.cofolder.data.model.entities.User
import com.dronina.cofolder.data.preferences.PreferenceManager
import com.dronina.cofolder.data.room.dao.UserDao
import com.dronina.cofolder.utils.other.*
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import kotlinx.coroutines.tasks.await
import org.json.JSONException
import org.json.JSONObject

class UserRepository(private val dao: UserDao) : BaseRepository() {
    private var requestsUpdateListener: RequestsUpdateListener? = null
    private var userUpdateListener: UserUpdateListener? = null

    interface UserUpdateListener {
        fun userUpdated(user: User?)
        fun allUsersUpdated(allUsers: List<User>)
    }

    interface RequestsUpdateListener {
        // first value - requests sent to me, second - sent by me
        fun requestsUpdated(requests: Pair<List<Request>, List<Request>>)
    }

    fun addAllUsersUpdateListener(listener: UserUpdateListener) {
        userUpdateListener = listener
        try {
            FirebaseSource.collection(USER)?.addSnapshotListener { snapshot, e ->
                snapshot?.toObjects<User>()?.let { listOfUsers ->
                    userUpdateListener?.allUsersUpdated(listOfUsers)
                }
            }
        } catch (e: Exception) {
        }
    }

    fun addUserUpdateListener(listener: UserUpdateListener) {
        userUpdateListener = listener
        try {
            FirebaseSource.userDatabaseRef()?.addSnapshotListener { snapshot, e ->
                userUpdateListener?.userUpdated(snapshot?.toObject<User>())
            }
        } catch (e: Exception) {
        }
    }

    fun addRequestsUpdateListener(listener: RequestsUpdateListener) {
        requestsUpdateListener = listener
        try {
            FirebaseSource.collection(REQUEST)?.addSnapshotListener { snapshot, e ->
                requestsUpdateListener?.requestsUpdated(onlyMyRequests(snapshot?.toObjects()))
            }
        } catch (e: Exception) {
        }
    }

    fun currentFirebaseUser(): FirebaseUser? {
        return FirebaseSource.currentUser()
    }

    fun signOutFromFirebaseUser() {
        return FirebaseSource.signOut()
    }

    fun subscribeOrUnsubscribeNotifications() {
        if (PreferenceManager.allowNotifications()) {
            FirebaseSource.subscribeToNotifications()
        } else {
            FirebaseSource.unsubscribeFromNotifications()
        }
    }

    fun signInIntent(context: Context): Intent? {
        return FirebaseSource.signInIntent(context)
    }

    override fun createUniqueId(): String {
        return "user_" + super.createUniqueId()
    }

    suspend fun getCurrentUserFromDb(): User? {
        var user = FirebaseSource.currentUserObject()
        if (user != null) {
            dao.insertUser(user)
        } else {
            val users = dao.getUsers()
            user = users.find {
                it.id == PreferenceManager.currentUserIdOffline()
            }
        }
        return user
    }

    suspend fun deleteProfilePicture() {
        FirebaseSource.currentUserObject()?.let { currentUser ->
            currentUser.photoUrl?.let { url ->
                if (url.isNotEmpty()) {
                    currentUser.photoUrl = ""
                    FirebaseSource.firestoreRef(USER, currentUser.id)?.set(currentUser)
                    FirebaseSource.deleteProfileImage(currentUser.id)
                }
            }
        }
    }

    suspend fun registerUserInDb(name: String, surname: String): Boolean {
        val id = currentFirebaseUser()?.uid
        val phone = currentFirebaseUser()?.phoneNumber

        return if (id == null || phone == null) false
        else {
            FirebaseSource.firestoreRef(USER, id)?.let { userRef ->
                val user = User(id, phone, name, surname)
                try {
                    userRef.set(user).await()
                    true
                } catch (e: Exception) {
                    false
                }
            } ?: run {
                false
            }
        }
    }

    fun updateUserInDb(newName: String, newSurname: String): Boolean {
        return try {
            FirebaseSource.userDatabaseRef()?.update(NAME_FIELD, newName)
            FirebaseSource.userDatabaseRef()?.update(SURNAME_FIELD, newSurname)
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getUserByPhone(phoneNo: String): User? {
        return FirebaseSource.collection(USER)?.get()?.await()?.toObjects<User>()
            ?.find { user ->
                user.phone == phoneNo
            }
    }

    suspend fun enteredOwnPhone(phone: String): Boolean {
        return FirebaseSource.currentUserObject()?.phone == phone
    }

    suspend fun createContactObjectFromUserObject(user: User): Contact {
        val requestSent = requestSent(user)
        return Contact(
            name = user.name,
            phone = user.phone.toString(),
            user = user,
            requestSentFromMe = requestSent.first,
            requestSentToMe = requestSent.second,
            isFriend = isFriend(user)
        )
    }

    suspend fun getFriends(friendsIds: List<String>?): ArrayList<User>? {
        val friends = ArrayList<User>()
        val allUsers = FirebaseSource.collection(USER)?.get()?.await()?.toObjects<User>()
        friendsIds?.forEach { friendId ->
            allUsers?.find { user ->
                user.id == friendId
            }?.let { user ->
                friends.add(user)
            }
        }
        return friends
    }

    // Returns pair of lists: first - requests sent to me, second - sent by me
    private fun onlyMyRequests(list: List<Request>?): Pair<List<Request>, List<Request>> {
        val toMe = ArrayList<Request>()
        val byMe = ArrayList<Request>()
        val myId = FirebaseSource.userId()
        list?.filter { request ->
            request.sender?.id == myId
        }?.let { requestByMe -> byMe.addAll(requestByMe) }
        list?.filter { request ->
            request.receiver?.id == myId
        }?.let { requestToMe -> toMe.addAll(requestToMe) }
        return Pair(toMe, byMe)
    }

    suspend fun sendRequest(receiver: User?): Boolean {
        return try {
            FirebaseSource.sendRequest(id = createUniqueRequestId(), receiver = receiver)
        } catch (e: Exception) {
            false
        }
    }

    suspend fun acceptRequest(sender: User?): Boolean {
        return try {
            FirebaseSource.acceptRequest(sender)
        } catch (e: Exception) {
            false
        }
    }

    suspend fun cancelRequest(receiver: User?): Boolean {
        return try {
            FirebaseSource.cancelRequest(receiver)
        } catch (e: Exception) {
            false
        }
    }

    suspend fun removeFriend(friend: User): Boolean {
        return try {
            FirebaseSource.removeFriend(friend)
        } catch (e: Exception) {
            false
        }
    }

    private fun createUniqueRequestId(): String {
        return "req_" + super.createUniqueId()
    }

    private suspend fun requestSent(user: User?): Pair<Boolean, Boolean> {
        var requestSentFromMe = false
        var requestSentToMe = false
        val requests = FirebaseSource.getRequests()
        val myId = FirebaseSource.userId()
        requests?.forEach { request ->
            if (request.sender == null || request.receiver == null || user == null) return@forEach
            if (request.sender?.id == myId && request.receiver?.id == user.id) {
                requestSentFromMe = true
                return@forEach
            } else if (request.sender?.id == user.id && request.receiver?.id == myId) {
                requestSentToMe = true
                return@forEach
            }
        }
        return Pair(requestSentFromMe, requestSentToMe)
    }

    private suspend fun isFriend(user: User?): Boolean {
        var isFriend = false

        FirebaseSource.currentUserObject()?.friends?.let { friends ->
            if (user == null || friends.isEmpty()) return@let
            friends.forEach { friend ->
                if (friend == user.id) {
                    isFriend = true
                    return@forEach
                }
            }
        }
        return isFriend
    }

    suspend fun uploadProfilePhoto(userId: String, uri: Uri) {
        FirebaseSource.firestoreRef(USER, userId)
            ?.update(PHOTO_URL_FIELD, FirebaseSource.loadProfileImage(uri, userId))
    }

    fun sendRequestToDevice(context: Context, receiver: User?, senderName: String) {
        if (receiver == null || receiver.id.isEmpty()) return
        val requestQueue: RequestQueue by lazy { Volley.newRequestQueue(context.applicationContext) }
        val notification = JSONObject()
        val notificationBody = JSONObject()
        try {
            notificationBody.put(
                TITLE,
                context.getString(R.string.request_title) + " " + senderName
            )
            notificationBody.put(MESSAGE, context.getString(R.string.request_message))
            notification.put(TO, TOPICS + receiver.id)
            notification.put(DATA, notificationBody)
        } catch (e: JSONException) {
        }

        val jsonObjectRequest = object : JsonObjectRequest(
            FCM_API, notification,
            Response.Listener {},
            Response.ErrorListener {}) {
            override fun getHeaders(): Map<String, String> {
                val params = HashMap<String, String>()
                params[AUTHORIZATION] = SERVER_KEY
                params[CONTENT_TYPE_PARAM] = CONTENT_TYPE
                return params
            }
        }
        requestQueue.add(jsonObjectRequest)
    }
}