package com.dronina.cofolder.data.model.other

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class ListItem(
    var text: String = "",
    var checked: Boolean = false
) : Parcelable {
    constructor() : this("", false)

    override fun toString(): String {
        return text
    }
}