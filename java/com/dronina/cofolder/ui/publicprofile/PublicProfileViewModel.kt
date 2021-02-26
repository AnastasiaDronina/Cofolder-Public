package com.dronina.cofolder.ui.publicprofile

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dronina.cofolder.data.model.other.Contact
import com.dronina.cofolder.data.model.entities.User
import com.dronina.cofolder.data.repository.UserRepository
import com.dronina.cofolder.data.room.CofolderDatabase
import com.dronina.cofolder.utils.extensions.ContactExtensions
import com.dronina.cofolder.utils.extensions.ifLet
import com.dronina.cofolder.utils.other.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PublicProfileViewModel(private val context: Context) : ViewModel() {
    private var repo: UserRepository? = null
    var view: PublicProfileFragment? = null
    var contactView: View? = null
    val currentUser = MutableLiveData<User>()
    val user = MutableLiveData<User>()
    var userText = MutableLiveData<String>()
    private var jobInProgress = false

    init {
        CofolderDatabase.getDatabase(context)?.userDao()?.let { dao ->
            repo = UserRepository(dao)
        }
    }

    fun onViewCreated(arguments: Bundle?) {
        arguments?.let {
            user.value = arguments.getParcelable(USER_BUNDLE)
            userText.value = user.value.toString()
            processUser()
        }
    }

    fun sendRequest() {
        processRequest(SEND_REQUEST)
    }

    fun acceptRequest() {
        processRequest(ACCEPT_REQUEST)
    }

    fun cancelRequest() {
        processRequest(CANCEL_REQUEST)
    }

    fun removeFriend() {
        processRequest(REMOVE_FRIEND)
    }

    private fun processRequest(type: Int) {
        if (jobInProgress) return
        jobInProgress = true
        view?.showProgress()
        viewModelScope.launch {
            repo?.let { repo ->
                user.value?.let { user ->
                    var successful = false
                    withContext(Dispatchers.IO) {
                        when (type) {
                            SEND_REQUEST -> successful = repo.sendRequest(user)
                            CANCEL_REQUEST -> successful = repo.cancelRequest(user)
                            ACCEPT_REQUEST -> successful = repo.acceptRequest(user)
                            REMOVE_FRIEND -> successful = repo.removeFriend(user)
                        }
                    }
                    if (successful) {
                        if (type == SEND_REQUEST) {
                            repo.sendRequestToDevice(
                                context = context,
                                receiver = user,
                                senderName = currentUser.value.toString()
                            )
                        }
                        processUser()
                    }
                }
            } ?: run {
                view?.networkError()
            }
        }
    }

    private fun processUser() {
        viewModelScope.launch {
            user.value?.let { user ->
                view?.showProgress()
                var contact: Contact? = null
                withContext(Dispatchers.IO) {
                    contact = repo?.createContactObjectFromUserObject(user)
                }
                view?.hideProgress()
                val viewExtensions = ContactExtensions(contactView)
                ifLet(
                    contact?.isFriend,
                    contact?.requestSentFromMe,
                    contact?.requestSentToMe
                ) { (isFriend, fromMe, toMe) ->
                    when {
                        isFriend -> viewExtensions.showRemoveFriendButton()
                        fromMe -> viewExtensions.showCancelRequestButton()
                        toMe -> viewExtensions.showAcceptButton()
                        else -> viewExtensions.showSendRequestButton()
                    }
                }
                jobInProgress = false
            }
        }
    }
}