package com.dronina.cofolder.data.model.entities

import android.os.Parcelable
import com.dronina.cofolder.utils.extensions.currentTime
import com.google.firebase.Timestamp
import kotlinx.android.parcel.Parcelize

@Parcelize
class Image(
    val id: String = "",
    var url: String = "",
    var creator: String = "",
    var dateOfCreation: Timestamp = currentTime()
) : Parcelable {
    constructor() : this("", "", "", currentTime())

    override fun equals(other: Any?): Boolean {
        return id == (other as Image).id
    }
}