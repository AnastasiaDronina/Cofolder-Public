package com.dronina.cofolder.data.room.typeconverters

import androidx.room.TypeConverter

class ListStringConverter {
    @TypeConverter
    fun fromListOfStrings(listOfString: List<String>): String {
        return listOfString.joinToString(",")
    }
    @TypeConverter
    fun toListOfStrings(flatStringList: String): List<String> {
        return flatStringList.split(",")
    }
}