package com.dronina.cofolder.data.model.entities

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.dronina.cofolder.data.room.typeconverters.DateTypeConverter
import com.dronina.cofolder.data.room.typeconverters.ListStringConverter
import com.dronina.cofolder.utils.other.NOTES_COLLECTION
import com.dronina.cofolder.utils.other.NO_COLOR
import com.dronina.cofolder.utils.extensions.currentTime
import com.google.firebase.Timestamp
import kotlinx.android.parcel.Parcelize

@Entity(tableName = NOTES_COLLECTION)
@Parcelize
data class NoteFile(
    @PrimaryKey(autoGenerate = false)
    override val id: String = "",
    override var name: String = "",
    override var creator: String = "",
    @TypeConverters(DateTypeConverter::class)
    override var dateOfLastEdit: Timestamp = currentTime(),
    @TypeConverters(ListStringConverter::class)
    override var contributors: List<String> = emptyList(),
    @TypeConverters(ListStringConverter::class)
    override var editors: List<String> = emptyList(),
    override var color: Int = NO_COLOR,
    override var offline: Boolean = false,
    var text: String = "",
    var lastEditTextPosition: Int = 0,
    var lastEditNamePosition: Int = 0,
    override var lastUserEdited: String = ""
) : Parcelable, BaseFile(
    id,
    name,
    creator,
    dateOfLastEdit,
    contributors,
    editors,
    color,
    offline,
    lastUserEdited
) {
    constructor() : this(
        "",
        "",
        "",
        currentTime(),
        emptyList(),
        emptyList(),
        NO_COLOR,
        false,
        "",
        0,
        0,
        ""
    )

    override fun toString(): String {
        return ("$name $text").toLowerCase()
    }
}