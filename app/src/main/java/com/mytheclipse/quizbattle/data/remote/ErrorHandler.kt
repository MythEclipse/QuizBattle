package com.mytheclipse.quizbattle.data.remote

import okhttp3.Interceptor
import okhttp3.Response
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Error Handler Interceptor
 * Converts OkHttp/Retrofit exceptions to custom ApiException
 */
class ErrorHandlerInterceptor : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        
        try {
            val response = chain.proceed(request)
            
            // Check if response is not successful
            if (!response.isSuccessful) {
                val code = response.code
                val message = response.message
                
                throw when (code) {
                    400 -> ApiException.BadRequestException(message)
                    401 -> ApiException.AuthException(message)
                    403 -> ApiException.ForbiddenException(message)
                    404 -> ApiException.NotFoundException(message)
                    409 -> ApiException.ConflictException(message)
                    in 500..599 -> ApiException.ServerException(code, message)
                    else -> ApiException.UnknownException(message)
                }
            }
            
            return response
            
        } catch (e: Exception) {
            throw when (e) {
                is ApiException -> e
                is UnknownHostException -> ApiException.NetworkException("No internet connection")
                is SocketTimeoutException -> ApiException.TimeoutException("Request timeout")
                is IOException -> ApiException.NetworkException("Network error: ${e.message}")
                else -> ApiException.UnknownException(e.message ?: "Unknown error")
            }
        }
    }
}

/**
 * Extension function to handle suspend API calls with proper error handling
 */
suspend fun <T> safeApiCall(
    apiCall: suspend () -> T
): Result<T> {
    return try {
        Result.success(apiCall())
    } catch (e: ApiException) {
        Result.failure(e)
    } catch (e: HttpException) {
        val code = e.code()
        val message = e.message()
        Result.failure(code.toApiException(message))
    } catch (e: UnknownHostException) {
        Result.failure(ApiException.NetworkException("No internet connection"))
    } catch (e: SocketTimeoutException) {
        Result.failure(ApiException.TimeoutException("Request timeout"))
    } catch (e: IOException) {
        Result.failure(ApiException.NetworkException("Network error: ${e.message}"))
    } catch (e: Exception) {
        Result.failure(ApiException.UnknownException(e.message ?: "Unknown error"))
    }
}

/**
 * Extension function to get user-friendly error message
 */
fun Throwable.toUserFriendlyMessage(): String {
    return when (this) {
        is ApiException.NetworkException -> "Tidak ada koneksi internet"
        is ApiException.TimeoutException -> "Koneksi timeout, coba lagi"
        is ApiException.ServerException -> "Server sedang bermasalah"
        is ApiException.AuthException -> "Sesi berakhir, silakan login kembali"
        is ApiException.NotFoundException -> "Data tidak ditemukan"
        is ApiException.BadRequestException -> "Permintaan tidak valid"
        is ApiException.ForbiddenException -> "Akses ditolak"
        is ApiException.ConflictException -> "Data konflik"
        else -> message ?: "Terjadi kesalahan"
    }
}
