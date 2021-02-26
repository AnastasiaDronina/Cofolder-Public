package com.dronina.cofolder.data.model.other

import android.os.Parcelable
import com.dronina.cofolder.data.model.entities.User
import kotlinx.android.parcel.Parcelize

@Parcelize
class Contributor(
    var user: User,
    var isMe: Boolean,
    var isCreator: Boolean,
    var isEditor: Boolean = false
) : Parcelable, User(
    user.id,
    user.phone,
    user.name,
    user.surname,
    user.photoUrl,
    user.notes,
    user.lists,
    user.folders,
    user.friends
)