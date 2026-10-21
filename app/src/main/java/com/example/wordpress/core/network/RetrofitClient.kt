package com.example.wordpressclient.core.network

import com.example.wordpressclient.BuildConfig
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

/**
 * RetrofitClient = nơi cấu hình Retrofit để gọi WordPress REST API.
 *
 * Dùng `object` nghĩa là chỉ có đúng một bản duy nhất (singleton) trong cả app,
 * nên Retrofit và OkHttp chỉ được tạo một lần rồi dùng lại.
 */
object RetrofitClient {

    /**
     * URL cơ sở của WordPress REST API.
     *
     * !!! Hãy thay bằng địa chỉ site của bạn.
     * Quy tắc của Retrofit: URL cơ sở PHẢI kết thúc bằng dấu "/".
     * Vì vậy các đường dẫn trong WordPressApi sẽ viết tương đối và không
     * bắt đầu bằng "/", ví dụ @GET("posts") để ra .../wp-json/wp/v2/posts.
     */
    const val BASE_URL = "https://your-site.com/wp-json/wp/v2/"

    /**
     * Bộ phân tích JSON (kotlinx.serialization).
     *
     * - ignoreUnknownKeys = true: bỏ qua các trường WordPress trả về mà DTO không khai báo
     *   (ví dụ guid, meta, _links). Thiếu dòng này, app sẽ báo lỗi khi gặp trường lạ.
     * - coerceInputValues = true: nếu server trả null cho trường có giá trị mặc định,
     *   dùng giá trị mặc định thay vì báo lỗi.
     */
    val json: Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    /**
     * OkHttp = tầng gửi nhận dữ liệu thật sự bên dưới Retrofit.
     */
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            // Thời gian chờ tối đa khi kết nối, đọc và ghi dữ liệu.
            // Mạng điện thoại thường chậm nên để 30 giây.
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .apply {
                // Chỉ ghi log request/response khi chạy bản debug,
                // tránh lộ dữ liệu ở bản phát hành.
                if (BuildConfig.DEBUG) {
                    addInterceptor(
                        HttpLoggingInterceptor().apply {
                            level = HttpLoggingInterceptor.Level.BASIC
                        }
                    )
                }
            }
            .build()
    }

    /**
     * Đối tượng Retrofit chính: gắn URL cơ sở, OkHttp và bộ phân tích JSON lại với nhau.
     */
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    /**
     * Tạo ra một service từ interface API.
     * Ví dụ sau này: RetrofitClient.createService<WordPressApi>()
     *
     * Tôi viết dạng tổng quát thay vì khai báo sẵn `val api: WordPressApi`,
     * vì WordPressApi.kt chưa tồn tại và file này sẽ không biên dịch được nếu tham chiếu tới nó.
     */
    inline fun <reified T> createService(): T = retrofitInstance().create(T::class.java)

    /**
     * Cho phép hàm inline ở trên truy cập `retrofit` (đang private).
     * @PublishedApi giúp hàm inline public gọi được hàm internal này.
     */
    @PublishedApi
    internal fun retrofitInstance(): Retrofit = retrofit
}