package com.example.wordpressclient.data.repository

import com.example.wordpressclient.core.common.AppError
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException

/**
 * Chạy một lệnh gọi API và gói kết quả vào Result, đổi mọi lỗi thành AppError.
 *
 * - CancellationException được ném lại để coroutine dừng đúng cách khi bị hủy
 *   (ví dụ người dùng thoát màn hình).
 * - Chỉ bắt Exception, không bắt Throwable: các lỗi nghiêm trọng của hệ thống
 *   (hết bộ nhớ, tràn ngăn xếp...) không bị nuốt mà vẫn làm app dừng để lộ ra lỗi thật.
 */
internal suspend fun <T> safeApiCall(block: suspend () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: Exception) {
        Result.failure(exception.toAppError())
    }

/** Phân loại lỗi thô thành AppError để UI hiện thông báo phù hợp. */
private fun Exception.toAppError(): AppError = when (this) {
    is AppError -> this
    // Mất mạng, hết giờ chờ, không phân giải được tên miền...
    is IOException -> AppError.Network(this)
    is HttpException -> if (code() == 404) {
        AppError.NotFound(this)
    } else {
        AppError.Server(code(), this)
    }
    // Dữ liệu trả về sai cấu trúc JSON.
    is SerializationException -> AppError.Parsing(this)
    else -> AppError.Unknown(this)
}