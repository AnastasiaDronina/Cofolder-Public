package com.dronina.cofolder.data.model.entities

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.dronina.cofolder.data.room.typeconverters.ListStringConverter
import com.dronina.cofolder.utils.other.USERS_COLLECTION
import kotlinx.android.parcel.Parcelize

@Entity (tableName = USERS_COLLECTION)
@Parcelize
open class User(
    @PrimaryKey(autoGenerate = false)
    val id: String = "",
    var phone: String? = "",
    var name: String = "",
    var surname: String? = "",
    var photoUrl: String? = "",
    @TypeConverters(ListStringConverter::class)
    var notes: List<String>? = emptyList(),
    @TypeConverters(ListStringConverter::class)
    var lists: List<String>? = emptyList(),
    @TypeConverters(ListStringConverter::class)
    var folders: List<String>? = emptyList(),
    @TypeConverters(ListStringConverter::class)
    var friends: List<String>? = emptyList()
) : Parcelable {
    constructor() : this("", "", "", "", "", emptyList(), emptyList(), emptyList(), emptyList())

    override fun toString(): String {
        return "$name $surname"
    }
}