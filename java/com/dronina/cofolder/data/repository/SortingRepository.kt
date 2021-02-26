package com.dronina.cofolder.data.repository

import com.dronina.cofolder.data.preferences.PreferenceManager

class SortingRepository {

    fun sortingMethod(data: Int): Int {
        return PreferenceManager.sorting(data)
    }

    fun setSortingMethod(data: Int, method: Int) {
        return PreferenceManager.setSorting(data, method)
    }

    fun showOnlyPrivate(data: Int): Boolean {
        return PreferenceManager.showOnlyPrivate(data)
    }

    fun setShowOnlyPrivate(data: Int, show: Boolean) {
        return PreferenceManager.setShowOnlyPrivate(data, show)
    }
}
