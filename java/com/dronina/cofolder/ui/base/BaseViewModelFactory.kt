package com.dronina.cofolder.ui.base

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dronina.cofolder.ui.addcontributors.AddContributorsViewModel
import com.dronina.cofolder.ui.addfriends.AddFriendsViewModel
import com.dronina.cofolder.ui.bottomsheet.BottomSheetViewModel
import com.dronina.cofolder.ui.changetheme.ChangeThemeViewModel
import com.dronina.cofolder.ui.editlist.EditListViewModel
import com.dronina.cofolder.ui.editnote.EditNoteViewModel
import com.dronina.cofolder.ui.feedback.FeedbackViewModel
import com.dronina.cofolder.ui.friends.FriendsViewModel
import com.dronina.cofolder.ui.imagedelails.ImageDetailsViewModel
import com.dronina.cofolder.ui.images.ImagesViewModel
import com.dronina.cofolder.ui.insidefolder.InsideFolderViewModel
import com.dronina.cofolder.ui.language.LanguageViewModel
import com.dronina.cofolder.ui.launch.LaunchViewModel
import com.dronina.cofolder.ui.lists.ListsViewModel
import com.dronina.cofolder.ui.main.MainViewModel
import com.dronina.cofolder.ui.notes.NotesViewModel
import com.dronina.cofolder.ui.profile.ProfileViewModel
import com.dronina.cofolder.ui.publicprofile.PublicProfileViewModel
import com.dronina.cofolder.ui.register.RegisterViewModel
import com.dronina.cofolder.ui.requests.RequestsViewModel
import com.dronina.cofolder.ui.settings.SettingsViewModel
import com.dronina.cofolder.ui.share.ShareViewModel
import com.dronina.cofolder.ui.sort.SortViewModel
import com.dronina.cofolder.utils.other.UNKNOWN_VIEW_MODEL
import java.lang.IllegalArgumentException

class BaseViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AddContributorsViewModel::class.java) ->
                AddContributorsViewModel() as T
            modelClass.isAssignableFrom(AddFriendsViewModel::class.java) ->
                AddFriendsViewModel(context) as T
            modelClass.isAssignableFrom(BottomSheetViewModel::class.java) ->
                BottomSheetViewModel(context) as T
            modelClass.isAssignableFrom(ChangeThemeViewModel::class.java) ->
                ChangeThemeViewModel(context) as T
            modelClass.isAssignableFrom(EditListViewModel::class.java) ->
                EditListViewModel(context) as T
            modelClass.isAssignableFrom(EditNoteViewModel::class.java) ->
                EditNoteViewModel(context) as T
            modelClass.isAssignableFrom(FeedbackViewModel::class.java) ->
                FeedbackViewModel() as T
            modelClass.isAssignableFrom(FriendsViewModel::class.java) ->
                FriendsViewModel(context) as T
            modelClass.isAssignableFrom(ImageDetailsViewModel::class.java) ->
                ImageDetailsViewModel(context) as T
            modelClass.isAssignableFrom(ImagesViewModel::class.java) ->
                ImagesViewModel(context) as T
            modelClass.isAssignableFrom(InsideFolderViewModel::class.java) ->
                InsideFolderViewModel(context) as T
            modelClass.isAssignableFrom(LanguageViewModel::class.java) ->
                LanguageViewModel(context) as T
            modelClass.isAssignableFrom(LaunchViewModel::class.java) ->
                LaunchViewModel(context) as T
            modelClass.isAssignableFrom(ListsViewModel::class.java) ->
                ListsViewModel(context) as T
            modelClass.isAssignableFrom(MainViewModel::class.java) ->
                MainViewModel(context) as T
            modelClass.isAssignableFrom(NotesViewModel::class.java) ->
                NotesViewModel(context) as T
            modelClass.isAssignableFrom(ProfileViewModel::class.java) ->
                ProfileViewModel(context) as T
            modelClass.isAssignableFrom(PublicProfileViewModel::class.java) ->
                PublicProfileViewModel(context) as T
            modelClass.isAssignableFrom(RegisterViewModel::class.java) ->
                RegisterViewModel(context) as T
            modelClass.isAssignableFrom(RequestsViewModel::class.java) ->
                RequestsViewModel(context) as T
            modelClass.isAssignableFrom(SettingsViewModel::class.java) ->
                SettingsViewModel(context) as T
            modelClass.isAssignableFrom(ShareViewModel::class.java) ->
                ShareViewModel() as T
            modelClass.isAssignableFrom(SortViewModel::class.java) ->
                SortViewModel() as T
            else -> throw IllegalArgumentException(UNKNOWN_VIEW_MODEL)
        }
    }
}