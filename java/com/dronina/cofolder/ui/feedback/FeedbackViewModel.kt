package com.dronina.cofolder.ui.feedback

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dronina.cofolder.data.repository.IssuesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FeedbackViewModel : ViewModel() {
    private var repo: IssuesRepository? = null
    var view: FeedbackFragment? = null

    init {
        repo = IssuesRepository()
    }


    fun sendIssue(text: String) {
        if (text.isNotEmpty()) {
            view?.showProgress()
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    repo?.createIssue(text)
                }
                view?.feedbackSent()
                view?.hideProgress()
            }
        }
    }
}