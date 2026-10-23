package com.example.wordpressclient.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [SavedPostEntity::class],
    version = 1,
    exportSchema = false
)
abstract class WordPressDatabase : RoomDatabase() {
    abstract fun savedPostDao(): SavedPostDao
}
