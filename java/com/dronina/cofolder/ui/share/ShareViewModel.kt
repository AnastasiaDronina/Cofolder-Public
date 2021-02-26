package com.dronina.cofolder.ui.share

import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dronina.cofolder.data.model.entities.BaseFile
import com.dronina.cofolder.data.model.other.Contributor
import com.dronina.cofolder.data.model.entities.User
import com.dronina.cofolder.data.repository.SharingRepository
import com.dronina.cofolder.utils.other.DATA_BUNDLE
import com.dronina.cofolder.utils.other.DATA_TYPE
import com.dronina.cofolder.utils.other.NOTHING
import com.dronina.cofolder.utils.extensions.connected
import com.dronina.cofolder.utils.extensions.ifLet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ShareViewModel : ViewModel(), SharingRepository.UpdateListener {
    private var repo: SharingRepository? = null
    var view: ShareFragment? = null
    val amIEditor = MutableLiveData<Boolean>()
    val contributors = MutableLiveData<ArrayList<Contributor>>()
    var contributorsInProgress = ArrayList<String>()
    var data: BaseFile? = null
    var dataType = NOTHING
    var openRvWithAnimation = true
    override val removingInProgress = contributorsInProgress.size > 0

    init {
        repo = SharingRepository()
    }

    fun addContributorOnClick() {
        amIEditor.value?.let { amIEditor ->
            if (!amIEditor || !connected()) {
                view?.networkError()
            } else {
                val bundle = Bundle()
                bundle.putParcelable(DATA_BUNDLE, data)
                view?.navigateAddContributorsPage(bundle)
            }
        }
    }

    fun onViewCreated(arguments: Bundle) {
        if (data == null || data?.id != arguments.getParcelable<BaseFile>(DATA_BUNDLE)?.id) {
            contributors.value = null
        }
        data = arguments.getParcelable(DATA_BUNDLE)
        dataType = arguments.getInt(DATA_TYPE)
        data?.let {
            repo?.addContributorsUpdateListener(this, data, dataType)
            amIEditor.value = repo?.amIEditor(data as BaseFile)
        }
    }

    fun removeContributor(contributor: Contributor?) {
        data?.id?.let { id ->
            contributor?.user?.let {
                viewModelScope.launch {
                    val mContributors = contributors.value
                    withContext(Dispatchers.IO) {
                        repo?.removeContributor(dataType, id, contributor as User)
                    }
                    mContributors?.remove(contributor)
                    contributors.value = mContributors
                    updateModel()
                    contributorsInProgress.remove(contributor.id)
                }
            }
        }
    }

    fun removeItemFromRv(contributor: Contributor?) {
        ifLet(contributor, contributors.value) { (contributor, list) ->
            contributorsInProgress.add((contributor as Contributor).id)
            val contributors = ArrayList<Contributor>()
            contributors.addAll(list as ArrayList<Contributor>)
            contributors.removeAll { it.id == contributor.id }
            this.contributors.value = contributors
        }
    }

    fun onCheckedChange(contributor: Contributor?, checked: Boolean?) {
        checked?.let {
            contributor?.isEditor = checked
            val id = data?.id
            contributor?.user?.let {
                id?.let {
                    viewModelScope.launch {
                        val contributors = contributors.value
                        withContext(Dispatchers.IO) {
                            repo?.editorUpdated(dataType, id, contributor)
                        }
                        contributors?.find { it.id == contributor.id }?.isEditor = checked
                        this@ShareViewModel.contributors.value = contributors
                        updateModel()
                    }
                }
            }
        }
    }

    override fun updated(data: BaseFile) {
        this.data = data
        getContributorsFromRepo()
    }

    fun getContributorsFromRepo() {
        repo?.let { repo ->
            data?.let { data ->
                view?.showProgress()
                var contributors = ArrayList<Contributor>()
                view?.getUsers()?.let { allUsers ->
                    contributors = repo.getContributors(
                        data.creator,
                        data.contributors,
                        data.editors,
                        allUsers
                    )
                }
                val contributorsAfterDelete = ArrayList<Contributor>()
                contributorsAfterDelete.addAll(contributors)
                contributors.forEach { contributor ->
                    if (contributorsInProgress.contains(contributor.id)) {
                        contributorsAfterDelete.removeAll { it.id == contributor.id }
                    }
                }
                this.contributors.value = contributorsAfterDelete
                view?.hideProgress()
            }
        }
    }

    private fun updateModel() {
        val newContributorsIds = ArrayList<String>()
        contributors.value?.let { contributors ->
            contributors.forEach { contributor ->
                newContributorsIds.add(contributor.id)
            }
        }
        data?.contributors = newContributorsIds
    }
}