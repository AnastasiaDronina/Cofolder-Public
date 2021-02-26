package com.dronina.cofolder.data.model.other

import android.os.Parcelable
import com.dronina.cofolder.data.model.entities.User
import kotlinx.android.parcel.Parcelize

@Parcelize
class Contact(
    var name: String = "",
    var phone: String = "",
    var user: User? = null,
    var requestSentFromMe: Boolean? = false,
    var requestSentToMe: Boolean? = false,
    var isFriend: Boolean? = false
) : Parcelable {
    constructor() : this("", "", null, false, false, false)

    override fun toString(): String {
        return "$name ${phone.trim()}"
    }
}