package com.dronina.cofolder.data.model.entities

import android.os.Parcelable
import androidx.room.Ignore
import com.dronina.cofolder.utils.other.NO_COLOR
import com.dronina.cofolder.utils.extensions.currentTime
import com.google.firebase.Timestamp
import kotlinx.android.parcel.Parcelize

/*
 * @Ignore annotation added, because the models are Room entity classes and
 * Room creates two columns with same names if fields are overridden, but
 * ignore annotation fixes the issue.
 */
@Parcelize
open class BaseFile(
    @Ignore
    open val id: String,
    @Ignore
    open var name: String,
    @Ignore
    open var creator: String,
    @Ignore
    open var dateOfLastEdit: Timestamp,
    @Ignore
    open var contributors: List<String>,
    @Ignore
    open var editors: List<String>,
    @Ignore
    open var color: Int,
    @Ignore
    open var offline: Boolean,
    @Ignore
    open var lastUserEdited: String
) : Parcelable {
    constructor() : this("", "", "", currentTime(), emptyList(), emptyList(), NO_COLOR, false, "")
}