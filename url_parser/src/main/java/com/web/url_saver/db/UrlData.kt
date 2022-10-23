package com.web.url_saver.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "url_data")
data class UrlData(
    @PrimaryKey(autoGenerate = false)
    val _id: Int = Int.MAX_VALUE,
    val admin: Boolean,
    val user: String
)