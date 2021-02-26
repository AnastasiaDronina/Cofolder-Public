package com.dronina.cofolder.data.room.dao

import androidx.room.*
import com.dronina.cofolder.data.model.entities.FolderFile
import com.dronina.cofolder.utils.other.COLOR_FIELD
import com.dronina.cofolder.utils.other.FOLDERS_COLLECTION
import com.dronina.cofolder.utils.other.ID_FIELD
import com.dronina.cofolder.utils.other.NAME_FIELD

@Dao
interface FolderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(folder: FolderFile)

    @Update
    suspend fun update(folder: FolderFile)

    @Delete
    suspend fun delete(folder: FolderFile)

    @Query("SELECT * FROM $FOLDERS_COLLECTION")
    suspend fun getAll(): List<FolderFile>

    @Query("DELETE FROM $FOLDERS_COLLECTION WHERE $ID_FIELD = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE $FOLDERS_COLLECTION SET $NAME_FIELD =:name WHERE $ID_FIELD = :id")
    suspend fun updateName(id: String, name: String)

    @Query("UPDATE $FOLDERS_COLLECTION SET $COLOR_FIELD =:color WHERE $ID_FIELD = :id")
    suspend fun updateColor(id: String, color: Int)
}