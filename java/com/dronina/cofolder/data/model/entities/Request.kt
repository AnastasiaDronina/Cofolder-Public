package com.dronina.cofolder.data.model.entities

import android.os.Parcelable
import com.dronina.cofolder.utils.extensions.currentTime
import com.google.firebase.Timestamp
import kotlinx.android.parcel.Parcelize

@Parcelize
class Request(
    val id: String = "",
    var sender: User? = null,
    var receiver: User? = null,
    var dateOfSend: Timestamp = currentTime()
) : Parcelable {
    constructor() : this("", null, null, currentTime())
}