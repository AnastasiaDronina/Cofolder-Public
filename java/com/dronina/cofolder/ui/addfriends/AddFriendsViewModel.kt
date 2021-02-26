package com.dronina.cofolder.ui.addfriends

import android.content.ContentResolver
import android.content.Context
import android.provider.ContactsContract
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dronina.cofolder.data.model.other.Contact
import com.dronina.cofolder.data.model.entities.User
import com.dronina.cofolder.data.repository.UserRepository
import com.dronina.cofolder.data.room.CofolderDatabase
import com.dronina.cofolder.utils.extensions.formatAsPhone
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddFriendsViewModel(context: Context) : ViewModel() {
    private var repo: UserRepository? = null
    var view: AddFriendsFragment? = null
    val contacts = MutableLiveData<List<Contact>>()
    val phone = MutableLiveData<String>()
    val contactName = MutableLiveData<String>()

    init {
        CofolderDatabase.getDatabase(context)?.userDao()?.let { dao ->
            repo = UserRepository(dao)
        }
        phone.value = ""
        contactName.value = ""
    }

    fun readContactsGranted(contentResolver: ContentResolver) {
        viewModelScope.launch {
            view?.showProgress()
            val list = ArrayList<Contact>()
            withContext(Dispatchers.IO) {
                val cursor = contentResolver.query(
                    ContactsContract.Contacts.CONTENT_URI, null, null, null, null
                )
                if ((cursor?.count ?: 0) > 0) {
                    while (cursor != null && cursor.moveToNext()) {
                        if (cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                            val innerCursor = contentResolver.query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                arrayOf(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))),
                                null
                            )
                            innerCursor?.let {
                                while (innerCursor.moveToNext()) {
                                    val phone = innerCursor.getString(
                                        innerCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                                    ).formatAsPhone()
                                    cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                                        ?.let { name ->
                                            if (list.find { it.phone == phone } == null) {
                                                list.add(Contact(name, phone))
                                            }
                                        }
                                }
                                innerCursor.close()
                            }
                        }
                    }
                }
                cursor?.close()
                list.sortBy { contact -> contact.name }
            }
            view?.hideProgress()
            contacts.value = list
        }
    }

    fun processPhone() {
        view?.processPhoneOnStart()
        phone.value?.let { phone ->
            if (phone.isEmpty()) {
                view?.processPhoneOnFailure()
            } else {
                this.phone.value = phone.formatAsPhone()
                repo?.let { repo ->
                    viewModelScope.launch {
                        var user: User? = null
                        var enteredOwnPhone = false
                        withContext(Dispatchers.IO) {
                            enteredOwnPhone = if (repo.enteredOwnPhone(phone)) {
                                true
                            } else {
                                user = repo.getUserByPhone(phone)
                                false
                            }
                        }
                        if (enteredOwnPhone) {
                            view?.enteredOwnPhone()
                        } else user?.let { user ->
                            view?.processPhoneOnSuccess(user)
                        } ?: run {
                            view?.processPhoneOnError()
                        }
                    }
                }
            }
        }
    }
}