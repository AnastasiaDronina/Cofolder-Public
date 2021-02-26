package com.dronina.cofolder.ui.profile

import android.R
import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dronina.cofolder.data.model.entities.User
import com.dronina.cofolder.data.repository.UserRepository
import com.dronina.cofolder.data.room.CofolderDatabase
import com.dronina.cofolder.utils.other.RC_PICK_IMAGES
import com.dronina.cofolder.utils.extensions.connected
import com.dronina.cofolder.utils.extensions.ifLet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileViewModel(context: Context) : ViewModel() {
    private var repo: UserRepository? = null
    var view: ProfileFragment? = null
    val name = MutableLiveData<String>()
    val surname = MutableLiveData<String>()
    var phone = MutableLiveData<String>()
    var profileImage = MutableLiveData<String>()
    private var currentUser = MutableLiveData<User>()
    private var cashedImage = MutableLiveData<String>()

    init {
        CofolderDatabase.getDatabase(context)?.userDao()?.let { dao ->
            repo = UserRepository(dao)
        }
    }

    fun profilePictureOnClick() {
        if (connected()) {
            view?.openFileChooser()
        } else {
            view?.networkError()
        }
    }

    fun deleteProfilePicture() {
        if (connected()) {
            view?.setPicture("")
            cashedImage.value = ""
            viewModelScope.launch {
                repo?.deleteProfilePicture()
            }
        } else {
            view?.networkError()
        }
    }

    fun saveName(newName: String) {
        if (newName.isNotEmpty()) {
            name.value = newName
            updateData()
        }
    }

    fun saveSurname(newSurname: String) {
        if (newSurname.isNotEmpty()) {
            surname.value = newSurname
            updateData()
        }
    }

    fun logout() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { repo?.signOutFromFirebaseUser() }
        }
        view?.onLogoutSuccess()
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!connected()) {
            view?.networkError()
        } else currentUser.value?.let { currentUser ->
            if (requestCode == RC_PICK_IMAGES && resultCode == Activity.RESULT_OK && null != R.attr.data) {
                val imageUri = data?.data
                cashedImage.value = imageUri.toString()
                view?.setPicture(imageUri.toString())
                imageUri?.let {
                    viewModelScope.launch {
                        withContext(Dispatchers.IO) {
                            repo?.uploadProfilePhoto(currentUser.id, imageUri)
                        }
                    }
                }
            }
        }
    }

    fun userUpdated(user: User?) {
        user?.let {
            currentUser.value = user
            if (name.value != user.name) {
                name.value = user.name
            }
            if (surname.value != user.surname) {
                surname.value = user.surname
            }
            if (phone.value != user.phone) {
                phone.value = user.phone
            }
            if (profileImage.value != user.photoUrl) {
                if (cashedImage.value == null) {
                    profileImage.value = user.photoUrl
                } else {
                    profileImage.value = cashedImage.value
                }
            }
        }
    }

    private fun updateData() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                ifLet(name.value, surname.value) { (name, surname) ->
                    repo?.updateUserInDb(name, surname)
                }
            }
        }
    }
}