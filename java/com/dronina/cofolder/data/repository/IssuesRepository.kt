package com.dronina.cofolder.data.repository

import com.dronina.cofolder.data.firebase.FirebaseSource
import com.dronina.cofolder.data.model.other.Issue
import com.dronina.cofolder.utils.other.ISSUE
import kotlinx.coroutines.tasks.await

class IssuesRepository : BaseRepository() {

    suspend fun createIssue(text: String) {
        val id = createUniqueId()

        FirebaseSource.userId()?.let { userId ->
            FirebaseSource.firestoreRef(ISSUE, id)?.set(Issue(id, userId, text))?.await()
        }
    }

}