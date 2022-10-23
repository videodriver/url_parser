package com.web.url_saver.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [UrlData::class], version = 1, exportSchema = false)
abstract class UrlDatabase : RoomDatabase() {
    abstract fun dao(): UrlDao

    companion object {
        @Volatile
        private var instance: UrlDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            instance ?: createDatabase(context).also { instance = it }
        }

        private fun createDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                UrlDatabase::class.java,
                "url_db.db"
            ).build()
    }
}