package com.dronina.cofolder.data.room.dao

import androidx.room.*
import com.dronina.cofolder.data.model.entities.User
import com.dronina.cofolder.utils.other.USERS_COLLECTION

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Update
    suspend fun updateUser(user: User)

    @Delete
    suspend fun deleteUser(user: User)

    @Query("SELECT * FROM $USERS_COLLECTION")
    suspend fun getUsers(): List<User>
}