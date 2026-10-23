package com.example.wordpressclient.di

import com.example.wordpressclient.data.repository.WordPressCategoryRepository
import com.example.wordpressclient.data.repository.WordPressPostRepository
import com.example.wordpressclient.data.repository.RoomSavedPostRepository
import com.example.wordpressclient.domain.repository.CategoryRepository
import com.example.wordpressclient.domain.repository.PostRepository
import com.example.wordpressclient.domain.repository.SavedPostRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * RepositoryModule = nơi "nối" interface với lớp cài đặt thật của nó.
 *
 * - @Module: đánh dấu đây là nơi chứa các chỉ dẫn cho Hilt.
 * - @InstallIn(SingletonComponent::class): các chỉ dẫn này có hiệu lực trong cả app.
 *
 * Dùng `abstract class` (thay vì `object` như NetworkModule) vì @Binds
 * yêu cầu hàm trừu tượng, chỉ khai báo, không có thân hàm.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindSavedPostRepository(
        impl: RoomSavedPostRepository
    ): SavedPostRepository


    @Binds
    @Singleton
    abstract fun bindCategoryRepository(
        impl: WordPressCategoryRepository
    ): CategoryRepository

    /**
     * Dòng này nói với Hilt: "Ai cần PostRepository thì đưa cho họ WordPressPostRepository."
     *
     * - Tham số `impl` là lớp cài đặt. Hilt tự tạo nó vì WordPressPostRepository
     *   có @Inject constructor.
     * - Kiểu trả về `PostRepository` là thứ mà nơi khác sẽ xin.
     * - @Singleton: chỉ tạo một bản duy nhất rồi dùng lại.
     */
    @Binds
    @Singleton
    abstract fun bindPostRepository(
        impl: WordPressPostRepository
    ): PostRepository
}