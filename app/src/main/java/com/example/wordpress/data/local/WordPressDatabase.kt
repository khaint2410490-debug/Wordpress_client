package com.example.wordpressclient.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        SavedPostEntity::class,
        CachedPostEntity::class,
        CachedPostListEntry::class
    ],
    version = 2,
    exportSchema = false
)
abstract class WordPressDatabase : RoomDatabase() {
    abstract fun savedPostDao(): SavedPostDao
    abstract fun cachedPostDao(): CachedPostDao
}
