package com.example.wordpressclient.core.network

import com.example.wordpress.BuildConfig
import com.example.wordpressclient.core.config.NetworkConfig
import com.example.wordpressclient.data.remote.WordPressApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * NetworkModule = module Hilt cung cấp các đối tượng mạng dùng chung.
 *
 * Mỗi hàm @Provides dạy Hilt cách tạo một đối tượng. Khi lớp khác (ví dụ
 * WordPressPostRepository) khai báo cần WordPressApi trong constructor,
 * Hilt sẽ tự gọi chuỗi các hàm bên dưới để tạo và truyền vào.
 *
 * - @Module: đánh dấu đây là nơi chứa các hàm cung cấp dependency.
 * - @InstallIn(SingletonComponent::class): các đối tượng sống suốt vòng đời của app.
 * - @Singleton: chỉ tạo một bản duy nhất rồi dùng lại.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /**
     * Bộ phân tích JSON.
     * - ignoreUnknownKeys: bỏ qua các trường WordPress trả về mà DTO không khai báo.
     * - coerceInputValues: nếu server trả null cho trường có giá trị mặc định,
     *   dùng giá trị mặc định thay vì báo lỗi.
     */
    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = NetworkConfig.IGNORE_UNKNOWN_JSON_KEYS
        coerceInputValues = NetworkConfig.COERCE_JSON_INPUT_VALUES
    }

    /**
     * OkHttp = tầng gửi nhận dữ liệu thật sự bên dưới Retrofit.
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            // Thời gian chờ tối đa khi kết nối, đọc và ghi dữ liệu.
            .connectTimeout(NetworkConfig.TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(NetworkConfig.TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(NetworkConfig.TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .apply {
                // Chỉ ghi log request/response ở bản debug.
                if (BuildConfig.DEBUG) {
                    addInterceptor(
                        HttpLoggingInterceptor().apply {
                            level = NetworkConfig.LOG_LEVEL
                        }
                    )
                }
            }
            .build()

    /**
     * Retrofit chính. Hilt tự truyền OkHttpClient và Json ở trên vào các tham số.
     */
    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        json: Json
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(NetworkConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    /**
     * Cung cấp WordPressApi. Retrofit tự sinh phần cài đặt cho interface này.
     */
    @Provides
    @Singleton
    fun provideWordPressApi(retrofit: Retrofit): WordPressApi =
        retrofit.create(WordPressApi::class.java)
}