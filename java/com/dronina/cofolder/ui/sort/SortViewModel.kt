package com.dronina.cofolder.ui.sort

import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dronina.cofolder.data.repository.SortingRepository
import com.dronina.cofolder.utils.other.SORT_BUNDLE

class SortViewModel : ViewModel() {
    private var repo: SortingRepository? = null
    var view: SortFragment? = null
    private var dataType: Int? = null
    private val sorting = MutableLiveData<Int>()
    private val showOnlyPrivate = MutableLiveData<Boolean>()

    init {
        repo = SortingRepository()
    }

    fun onViewCreated(arguments: Bundle?) {
        arguments?.getInt(SORT_BUNDLE)?.let { dataType ->
            this.dataType = dataType
            sorting.value = repo?.sortingMethod(dataType)
            showOnlyPrivate.value = repo?.showOnlyPrivate(dataType)
            sorting.value?.let { sorting ->
                view?.setRadioButtonChecked(sorting)
            }
            showOnlyPrivate.value?.let { showOnlyPrivate ->
                view?.setCheckbox(showOnlyPrivate)
            }
        }
    }

    fun saveSort() {
        dataType?.let { dataType ->
            sorting.value?.let { sorting ->
                repo?.setSortingMethod(dataType, sorting)
            }
            showOnlyPrivate.value?.let { showOnlyPrivate ->
                repo?.setShowOnlyPrivate(dataType, showOnlyPrivate)
            }
            view?.updateSorting(dataType)
            view?.navigateUp()
        }
    }

    fun checked(sortingMethod: Int) {
        this.sorting.value = sortingMethod
    }

    fun showOnlyPrivate(showOnlyPrivate: Boolean) {
        this.showOnlyPrivate.value = showOnlyPrivate
    }

}
