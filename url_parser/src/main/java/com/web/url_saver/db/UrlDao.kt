package com.web.url_saver.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UrlDao {

    @Query("SELECT COUNT(*) FROM url_data")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: UrlData)

    @Query("SELECT admin FROM url_data LIMIT 1")
    suspend fun getAdmin(): Boolean

    @Query("SELECT user FROM url_data LIMIT 1")
    suspend fun getUser(): String

    @Query("SELECT * FROM url_data LIMIT 1")
    suspend fun getEntity(): UrlData
}