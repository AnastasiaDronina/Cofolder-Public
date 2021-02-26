package com.dronina.cofolder.ui.requests

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dronina.cofolder.data.firebase.FirebaseSource
import com.dronina.cofolder.data.model.entities.Request
import com.dronina.cofolder.data.repository.UserRepository
import com.dronina.cofolder.data.room.CofolderDatabase
import com.dronina.cofolder.utils.other.USER_BUNDLE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RequestsViewModel(context: Context) : ViewModel() {
    private var repo: UserRepository? = null
    var view: RequestsFragment? = null
    val showRequestsToMe = MutableLiveData<Boolean>()
    var showRequestsByMe = MutableLiveData<Boolean>()

    init {
        showRequestsToMe.value = true
        showRequestsByMe.value = true
    }

    init {
        CofolderDatabase.getDatabase(context)?.userDao()?.let { dao ->
            repo = UserRepository(dao)
        }
    }

    fun showRequestsToMeOnClick() {
        showRequestsToMe.value?.let {
            showRequestsToMe.value = !it
        }
    }

    fun showRequestsByMeOnClick() {
        showRequestsByMe.value?.let {
            showRequestsByMe.value = !it
        }
    }

    fun onShortClick(request: Request?) {
        if (request?.sender == null || request.receiver == null) return
        val bundle = Bundle()
        if (request.sender?.id == FirebaseSource.userId()) {
            bundle.putParcelable(USER_BUNDLE, request.receiver)
            view?.navigatePublicProfile(request.receiver.toString(), bundle)
        } else if (request.receiver?.id == FirebaseSource.userId()) {
            bundle.putParcelable(USER_BUNDLE, request.sender)
            view?.navigatePublicProfile(request.sender.toString(), bundle)
        }
    }

    fun cancelRequestOnClick(request: Request?) {
        request?.let {
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    repo?.cancelRequest(receiver = request.receiver)
                }
            }
        }
    }

    fun acceptRequestOnClick(request: Request?) {
        request?.let {
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    repo?.acceptRequest(sender = request.sender)
                }
            }
        }
    }
}