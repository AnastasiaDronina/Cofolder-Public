package com.dronina.cofolder.ui.friends

import android.content.Context
import androidx.lifecycle.ViewModel
import com.dronina.cofolder.data.repository.UserRepository
import com.dronina.cofolder.data.room.CofolderDatabase

class FriendsViewModel(context: Context) : ViewModel() {
    private var repo: UserRepository? = null
    var view: FriendsFragment? = null

    init {
        CofolderDatabase.getDatabase(context)?.userDao()?.let { dao ->
            repo = UserRepository(dao)
        }
    }
}