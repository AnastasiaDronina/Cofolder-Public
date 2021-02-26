package com.dronina.cofolder.ui.addcontributors

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dronina.cofolder.data.model.entities.BaseFile
import com.dronina.cofolder.data.model.entities.User
import com.dronina.cofolder.data.repository.SharingRepository
import com.dronina.cofolder.ui.base.BaseFragment
import com.dronina.cofolder.ui.main.MainViewModel
import com.dronina.cofolder.ui.share.ShareViewModel
import com.dronina.cofolder.utils.other.NOTHING
import com.dronina.cofolder.utils.extensions.connected
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddContributorsViewModel : ViewModel() {
    private var repo = SharingRepository()

    var view: BaseFragment? = null
    var data: BaseFile? = null
    var dataType: Int? = null
    val currentContributors = MutableLiveData<List<Pair<User, Boolean>>>()
    var openRvWithAnimation = true

    fun onViewCreated(friends: List<User>?) {
        data?.let { file ->
            friends?.let {
                getContributorsFromRepo(file.contributors, friends)
            }
        }
    }

    fun sync(shareViewModel: ShareViewModel, mainViewModel: MainViewModel): Boolean {
        return if (data?.id != shareViewModel.data?.id || data?.contributors != shareViewModel.data?.contributors) {
            data = shareViewModel.data
            dataType = shareViewModel.dataType
            onViewCreated(mainViewModel.currentFriends.value)
            true
        } else false
    }

    fun onClick(contributor: Pair<User, Boolean>?) {
        currentContributors.value?.let { currentContributors ->
            if (contributor == null) return
            val contributors = ArrayList<Pair<User, Boolean>>()
            currentContributors.forEach { pair ->
                if (pair.first.id == contributor.first.id) {
                    contributors.add(Pair(contributor.first, !contributor.second))
                } else {
                    contributors.add(pair)
                }
            }
            this.currentContributors.value = contributors
        }
    }

    fun save() {
        if (connected()) {
            currentContributors.value?.let { currentContributors ->
                if ((currentContributors.filter { pair -> pair.second }).isNotEmpty()) {
                    val contributors = ArrayList<User>()
                    currentContributors.forEach { contributor ->
                        if (contributor.second) {
                            contributors.add(contributor.first)
                        }
                    }
                    for (i in 0 until contributors.size) {
                        viewModelScope.launch {
                            view?.showProgress()
                            withContext(Dispatchers.IO) {
                                repo.addContributor(dataType, contributors[i].id, data?.id)
                            }
                            if (i == contributors.size - 1) {
                                view?.hideProgress()
                                navigateUp()
                            }
                        }
                    }
                } else navigateUp()
            }
        } else {
            view?.networkError()
            navigateUp()
        }
    }

    private fun navigateUp() {
        view?.navigateUp()
        data = null
        dataType = NOTHING
    }

    private fun getContributorsFromRepo(contributorsIds: List<String>, friends: List<User>) {
        repo.getContributorsToAdd(contributorsIds, friends)?.let { contributors ->
            currentContributors.value = contributors
        }
    }
}