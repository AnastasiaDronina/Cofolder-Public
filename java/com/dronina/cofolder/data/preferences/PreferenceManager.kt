package com.dronina.cofolder.data.preferences

import android.content.Context
import com.dronina.cofolder.CofolderApp
import com.dronina.cofolder.utils.other.*

object PreferenceManager {
    private val preferences =
        CofolderApp.context?.getSharedPreferences(PREFERENCES_KEY, Context.MODE_PRIVATE)

    fun firstLaunch(): Boolean {
        return preferences?.let {
            preferences.getBoolean(FIRST_LAUNCH, true)
        } ?: run {
            true
        }
    }

    fun setFirstLaunch(firstLaunch: Boolean) {
        preferences?.edit()?.putBoolean(FIRST_LAUNCH, firstLaunch)?.apply()
    }

    fun appTheme(): Int? {
        return preferences?.getInt(APP_THEME, DEFAULT_THEME)
    }

    fun setAppTheme(theme: Int) {
        preferences?.edit()?.putInt(APP_THEME, theme)?.apply()
    }

    fun allowNotifications(): Boolean {
        return preferences?.let {
            preferences.getBoolean(ALLOW_NOTIFICATIONS, true)
        } ?: run {
            true
        }
    }

    fun currentLanguage(): String? {
        return preferences?.getString(LANGUAGE, DEFAULT_LANGUAGE)
    }

    fun setCurrentLanguage(lang: String) {
        preferences?.edit()?.putString(LANGUAGE, lang)?.apply()
    }

    fun setAllowNotifications(allow: Boolean) {
        preferences?.edit()?.putBoolean(ALLOW_NOTIFICATIONS, allow)?.apply()
    }

    fun currentUserIdOffline(): String? {
        return preferences?.getString(CURRENT_USER_ID_OFFLINE, null)
    }

    fun setCurrentUserIdOffline(id: String) {
        preferences?.edit()?.putString(CURRENT_USER_ID_OFFLINE, id)?.apply()
    }

    fun showAsGrid(dataType: Int): Boolean {
        return preferences?.let {
            when (dataType) {
                NOTE -> preferences.getBoolean(SHOW_NOTES_AS_GRID, false)
                LIST -> preferences.getBoolean(SHOW_LISTS_AS_GRID, false)
                FOLDER -> preferences.getBoolean(SHOW_FOLDERS_AS_GRID, false)
                else -> false
            }
        } ?: run {
            false
        }
    }

    fun setShowAsGrid(dataType: Int, asGrid: Boolean) {
        when (dataType) {
            NOTE -> preferences?.edit()?.putBoolean(SHOW_NOTES_AS_GRID, asGrid)?.apply()
            LIST -> preferences?.edit()?.putBoolean(SHOW_LISTS_AS_GRID, asGrid)?.apply()
            FOLDER -> preferences?.edit()?.putBoolean(SHOW_FOLDERS_AS_GRID, asGrid)?.apply()
        }
    }

    fun sorting(dataType: Int): Int {
        return preferences?.let {
            when (dataType) {
                NOTE -> preferences.getInt(SORTING_NOTES, BY_DEFAULT)
                LIST -> preferences.getInt(SORTING_LISTS, BY_DEFAULT)
                FOLDER -> preferences.getInt(SORTING_FOLDER, BY_DEFAULT)
                else -> BY_DEFAULT
            }
        } ?: run {
            BY_DEFAULT
        }
    }

    fun setSorting(dataType: Int, sorting: Int) {
        when (dataType) {
            NOTE -> preferences?.edit()?.putInt(SORTING_NOTES, sorting)?.apply()
            LIST -> preferences?.edit()?.putInt(SORTING_LISTS, sorting)?.apply()
            FOLDER -> preferences?.edit()?.putInt(SORTING_FOLDER, sorting)?.apply()
        }
    }

    fun showOnlyPrivate(dataType: Int): Boolean {
        return preferences?.let {
            when (dataType) {
                NOTE -> preferences.getBoolean(NOTES_SHOW_ONLY_PRIVATE, false)
                LIST -> preferences.getBoolean(LISTS_SHOW_ONLY_PRIVATE, false)
                FOLDER -> preferences.getBoolean(FOLDERS_SHOW_ONLY_PRIVATE, false)
                else -> false
            }
        } ?: run {
            false
        }
    }

    fun setShowOnlyPrivate(dataType: Int, showOnlyPrivate: Boolean) {
        when (dataType) {
            NOTE -> preferences?.edit()?.putBoolean(NOTES_SHOW_ONLY_PRIVATE, showOnlyPrivate)
                ?.apply()
            LIST -> preferences?.edit()?.putBoolean(LISTS_SHOW_ONLY_PRIVATE, showOnlyPrivate)
                ?.apply()
            FOLDER -> preferences?.edit()?.putBoolean(FOLDERS_SHOW_ONLY_PRIVATE, showOnlyPrivate)
                ?.apply()
        }
    }
}