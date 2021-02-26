package com.dronina.cofolder.ui.changetheme

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dronina.cofolder.data.preferences.PreferenceManager
import com.dronina.cofolder.utils.other.DARK_THEME
import com.dronina.cofolder.utils.other.DEFAULT_THEME
import com.dronina.cofolder.utils.other.LIGHT_THEME

class ChangeThemeViewModel(private val context: Context) : ViewModel() {

    val currentTheme = MutableLiveData<Int>()

    init {
        currentTheme.value = PreferenceManager.appTheme()
    }

    fun checked(theme: Int) {
        currentTheme.value = theme
    }

    fun saveTheme() {
        currentTheme.value?.let { theme ->
            PreferenceManager.setAppTheme(theme)
            when (theme) {
                LIGHT_THEME -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                DARK_THEME -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                DEFAULT_THEME -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }
    }
}