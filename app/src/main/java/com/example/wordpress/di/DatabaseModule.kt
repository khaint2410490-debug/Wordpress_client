package com.example.wordpressclient.di

import android.content.Context
import androidx.room.Room
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
    @Provides
    @Singleton
    fun provideWordPressDatabase(
        @ApplicationContext context: Context
    ): WordPressDatabase =
        Room.databaseBuilder(
            context,
            WordPressDatabase::class.java,
            "wordpress-client.db"
        ).build()

    @Provides
    fun provideSavedPostDao(database: WordPressDatabase): SavedPostDao =
        database.savedPostDao()
}
