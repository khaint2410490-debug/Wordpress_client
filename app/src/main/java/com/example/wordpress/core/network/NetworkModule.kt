package com.example.wordpressclient.core.network

import com.example.wordpressclient.BuildConfig
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
     * URL cơ sở của WordPress REST API.
     *
     * !!! Hãy thay bằng địa chỉ site của bạn, và phải kết thúc bằng dấu "/"
     * (quy tắc của Retrofit). Giá trị này trùng với BASE_URL trong RetrofitClient,
     * nên bạn chỉ cần giữ một nơi khi quay lại xử lý điểm 2.
     */
    private const val BASE_URL = "https://your-site.com/wp-json/wp/v2/"

    /**
     * Bộ phân tích JSON.
     * - ignoreUnknownKeys: bỏ qua các trường WordPress trả về mà DTO không khai báo.
     * - coerceInputValues: nếu server trả null cho trường có giá trị mặc định,
     *   dùng giá trị mặc định thay vì báo lỗi.
     */
    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    /**
     * OkHttp = tầng gửi nhận dữ liệu thật sự bên dưới Retrofit.
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            // Thời gian chờ tối đa khi kết nối, đọc và ghi dữ liệu.
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .apply {
                // Chỉ ghi log request/response ở bản debug.
                if (BuildConfig.DEBUG) {
                    addInterceptor(
                        HttpLoggingInterceptor().apply {
                            level = HttpLoggingInterceptor.Level.BASIC
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
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    /**
     * Cung cấp WordPressApi. Retrofit tự sinh phần cài đặt cho interface này.
     */
    @Provides
    @Singleton
    fun provideWordPressApi(retrofit: Retrofit): `WordPressApi.kt` =
        retrofit.create(`WordPressApi.kt`::class.java)
}