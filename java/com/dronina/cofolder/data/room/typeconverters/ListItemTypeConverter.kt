package com.dronina.cofolder.data.room.typeconverters

import androidx.room.TypeConverter
import com.dronina.cofolder.data.model.other.ListItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type


class ListItemTypeConverter {
    @TypeConverter
    fun fromListItems(optionValues: ArrayList<ListItem?>?): String? {
        if (optionValues == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object :
            TypeToken<ArrayList<ListItem?>?>() {}.type
        return gson.toJson(optionValues, type)
    }

    @TypeConverter
    fun toListItems(listItemsString: String?): ArrayList<ListItem>? {
        if (listItemsString == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object :
            TypeToken<ArrayList<ListItem?>?>() {}.type
        return gson.fromJson<ArrayList<ListItem>>(listItemsString, type)
    }
}