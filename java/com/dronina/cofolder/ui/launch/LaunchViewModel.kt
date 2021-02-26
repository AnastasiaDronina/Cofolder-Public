package com.dronina.cofolder.ui.launch

import android.app.Activity
import android.content.Context
import androidx.lifecycle.*
import com.dronina.cofolder.data.repository.UserRepository
import com.dronina.cofolder.data.model.entities.User
import com.dronina.cofolder.data.preferences.PreferenceManager
import com.dronina.cofolder.data.room.CofolderDatabase
import com.dronina.cofolder.utils.other.RC_SIGN_IN
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LaunchViewModel(context: Context) : ViewModel() {
    private var repo: UserRepository? = null
    var view: LaunchActivity? = null

    init {
        CofolderDatabase.getDatabase(context)?.userDao()?.let { dao ->
            repo = UserRepository(dao)
        }
    }

    fun onActivityStarted() {
        if (PreferenceManager.firstLaunch()) {
            view?.showIntro()
        } else {
            chooseActivity()
        }
    }

    fun onActivityResult(requestCode: Int, requestResult: Int) {
        if (requestCode == RC_SIGN_IN && requestResult == Activity.RESULT_OK) {
            checkUserInDatabase()
        } else {
            onActivityStarted()
        }
    }

    private fun checkUserInDatabase() {
        viewModelScope.launch {
            var result: User? = null
            withContext(Dispatchers.IO) {
                result = repo?.getCurrentUserFromDb()
            }
            result?.let {
                view?.navigateMain()
            } ?: run {
                view?.navigateRegister()
            }
        }
    }


    private fun chooseActivity() {
        repo?.currentFirebaseUser()?.let { user ->
            checkUserInDatabase()
        } ?: run {
            view?.navigateLogin()
        }
    }
}