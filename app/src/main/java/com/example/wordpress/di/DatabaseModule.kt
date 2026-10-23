package com.example.wordpressclient.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.wordpressclient.data.local.CachedPostDao
import com.example.wordpressclient.data.local.SavedPostDao
import com.example.wordpressclient.data.local.WordPressDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS cached_posts (
                    id INTEGER NOT NULL PRIMARY KEY,
                    title TEXT NOT NULL,
                    excerpt TEXT NOT NULL,
                    contentHtml TEXT NOT NULL,
                    date TEXT NOT NULL,
                    link TEXT NOT NULL,
                    featuredImageUrl TEXT,
                    authorName TEXT,
                    authorAvatarUrl TEXT,
                    cachedAt INTEGER NOT NULL
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS cached_post_list (
                    postId INTEGER NOT NULL PRIMARY KEY,
                    position INTEGER NOT NULL
                )
                """.trimIndent()
            )
        }
    }

    @Provides
    @Singleton
    fun provideWordPressDatabase(
        @ApplicationContext context: Context
    ): WordPressDatabase =
        Room.databaseBuilder(
            context,
            WordPressDatabase::class.java,
            "wordpress-client.db"
        )
            .addMigrations(MIGRATION_1_2)
            .build()

    @Provides
    fun provideSavedPostDao(database: WordPressDatabase): SavedPostDao =
        database.savedPostDao()

    @Provides
    fun provideCachedPostDao(database: WordPressDatabase): CachedPostDao =
        database.cachedPostDao()
}
