package com.dronina.cofolder.ui.settings

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dronina.cofolder.data.preferences.PreferenceManager
import com.dronina.cofolder.data.repository.UserRepository
import com.dronina.cofolder.data.room.CofolderDatabase

class SettingsViewModel(context: Context) : ViewModel() {
    private var repo: UserRepository? = null
    val allowNotifications = MutableLiveData<Boolean>()

    init {
        CofolderDatabase.getDatabase(context)?.userDao()?.let { dao ->
            repo = UserRepository(dao)
        }
        allowNotifications.value = PreferenceManager.allowNotifications()
    }

    fun checkedChanged(checked: Boolean) {
        allowNotifications.value = checked
        PreferenceManager.setAllowNotifications(checked)
        repo?.subscribeOrUnsubscribeNotifications()
    }
}