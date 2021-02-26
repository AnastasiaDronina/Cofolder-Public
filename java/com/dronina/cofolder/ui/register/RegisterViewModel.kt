package com.dronina.cofolder.ui.register

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dronina.cofolder.data.repository.UserRepository
import com.dronina.cofolder.data.room.CofolderDatabase
import com.dronina.cofolder.utils.extensions.ifLet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterViewModel(context: Context) : ViewModel() {
    private var repo: UserRepository? = null
    var view: RegisterActivity? = null
    val inputName = MutableLiveData<String>()
    val inputSurname = MutableLiveData<String>()

    init {
        CofolderDatabase.getDatabase(context)?.userDao()?.let { dao ->
            repo = UserRepository(dao)
        }
        inputName.value = ""
        inputSurname.value = ""
    }

    fun saveData() {
        ifLet(inputName.value, inputSurname.value) { (name, surname) ->
            if (name.isEmpty() || surname.isEmpty()) {
                view?.hideProgress()
                view?.userGeneratedError()
            } else viewModelScope.launch {
                view?.showProgress()
                repo?.let { repo ->
                    var successfull = false
                    withContext(Dispatchers.IO) {
                        successfull = repo.registerUserInDb(name, surname)
                    }
                    if (successfull) {
                        view?.success()
                    } else {
                        view?.hideProgress()
                        view?.networkError()
                    }
                } ?: run {
                    view?.hideProgress()
                    view?.networkError()
                }
            }
        }
    }
}