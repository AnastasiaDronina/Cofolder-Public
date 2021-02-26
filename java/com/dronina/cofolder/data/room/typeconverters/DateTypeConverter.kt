package com.dronina.cofolder.data.room.typeconverters

import androidx.room.TypeConverter
import com.google.firebase.Timestamp
import java.util.*

class DateTypeConverter {
    @TypeConverter
    fun fromTimestamp(value: Long?): Timestamp? {
        return if (value == null) null else Timestamp(Date(value))
    }

    @TypeConverter
    fun toTimestamp(timestamp: Timestamp?): Long? {
        return timestamp?.toDate()?.time
    }
}