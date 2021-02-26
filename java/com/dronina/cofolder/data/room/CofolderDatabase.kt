package com.dronina.cofolder.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.dronina.cofolder.data.model.entities.FolderFile
import com.dronina.cofolder.data.model.entities.ListFile
import com.dronina.cofolder.data.model.entities.NoteFile
import com.dronina.cofolder.data.model.entities.User
import com.dronina.cofolder.data.room.dao.FolderDao
import com.dronina.cofolder.data.room.dao.ListDao
import com.dronina.cofolder.data.room.dao.NoteDao
import com.dronina.cofolder.data.room.dao.UserDao
import com.dronina.cofolder.data.room.typeconverters.DateTypeConverter
import com.dronina.cofolder.data.room.typeconverters.ListItemTypeConverter
import com.dronina.cofolder.data.room.typeconverters.ListStringConverter
import com.dronina.cofolder.utils.other.DATABASE

@Database(entities = [User::class, NoteFile::class, ListFile::class, FolderFile::class], version = 1)
@TypeConverters(DateTypeConverter::class, ListStringConverter::class, ListItemTypeConverter::class)
abstract class CofolderDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun noteDao(): NoteDao
    abstract fun listDao(): ListDao
    abstract fun folderDao(): FolderDao

    companion object {
        var INSTANCE: CofolderDatabase? = null

        fun getDatabase(context: Context): CofolderDatabase? {
            if (INSTANCE == null) {
                synchronized(CofolderDatabase::class) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        CofolderDatabase::class.java,
                        DATABASE
                    ).build()
                }
            }
            return INSTANCE
        }

        fun destroyDatabase() {
            INSTANCE = null
        }
    }
}