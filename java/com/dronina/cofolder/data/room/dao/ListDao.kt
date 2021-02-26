package com.dronina.cofolder.data.room.dao

import androidx.room.*
import com.dronina.cofolder.data.model.entities.ListFile
import com.dronina.cofolder.utils.other.*

@Dao
interface ListDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(list: ListFile)

    @Update
    suspend fun update(list: ListFile)

    @Delete
    suspend fun delete(list: ListFile)

    @Query("SELECT * FROM $LISTS_COLLECTION")
    suspend fun getAll(): List<ListFile>

    @Query("DELETE FROM $LISTS_COLLECTION WHERE $ID_FIELD = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE $LISTS_COLLECTION SET $NAME_FIELD =:name WHERE $ID_FIELD = :id")
    suspend fun updateName(id: String, name: String)

    @Query("UPDATE $LISTS_COLLECTION SET $ITEMS_FIELD =:items WHERE $ID_FIELD = :id")
    suspend fun updateItems(id: String, items: String)

    @Query("UPDATE $LISTS_COLLECTION SET $COLOR_FIELD =:color WHERE $ID_FIELD = :id")
    suspend fun updateColor(id: String, color: Int)
}