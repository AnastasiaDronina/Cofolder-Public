package com.dronina.cofolder.data.room.dao

import androidx.room.*
import com.dronina.cofolder.data.model.entities.NoteFile
import com.dronina.cofolder.utils.other.*

@Dao
interface NoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: NoteFile)

    @Update
    suspend fun update(note: NoteFile)

    @Delete
    suspend fun delete(note: NoteFile)

    @Query("SELECT * FROM $NOTES_COLLECTION")
    suspend fun getAll(): List<NoteFile>

    @Query("DELETE FROM $NOTES_COLLECTION WHERE $ID_FIELD = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE $NOTES_COLLECTION SET $NAME_FIELD =:name WHERE $ID_FIELD = :id")
    suspend fun updateName(id: String, name: String)

    @Query("UPDATE $NOTES_COLLECTION SET $TEXT_FILED =:text WHERE $ID_FIELD = :id")
    suspend fun updateText(id: String, text: String)

    @Query("UPDATE $NOTES_COLLECTION SET $COLOR_FIELD =:color WHERE $ID_FIELD = :id")
    suspend fun updateColor(id: String, color: Int)
}