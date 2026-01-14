package com.mytheclipse.quizbattle.data.remote

import com.mytheclipse.quizbattle.utils.AppLogger
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.internal.http.promisesBody
import okio.Buffer
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit

/**
 * ApiLoggingInterceptor
 * Logs request and response metadata using centralized AppLogger.
 * Only active in DEBUG builds.
 */
class ApiLoggingInterceptor(private val tag: String = "API") : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val requestStart = System.nanoTime()

        // Log request
        val method = request.method
        val url = request.url.toString()
        AppLogger.Network.apiRequest(method, url)

        // Log request body if present
        val requestBody = request.body
        if (requestBody != null) {
            try {
                val buffer = Buffer()
                requestBody.writeTo(buffer)
                val charset = requestBody.contentType()?.charset(Charset.forName("UTF-8")) 
                    ?: Charset.forName("UTF-8")
                val body = buffer.readString(charset)
                AppLogger.log(AppLogger.LogLevel.DEBUG, "Network", "Request body: ${body.take(200)}...")
            } catch (e: Exception) {
                // Cannot read body
            }
        }

        // Execute request
        val response: Response
        try {
            response = chain.proceed(request)
        } catch (e: Exception) {
            AppLogger.Network.apiError("$method $url", e.message ?: "Unknown error", e)
            throw e
        }

        val tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - requestStart)
        
        // Log response
        AppLogger.Network.apiResponse(url, response.code, tookMs)
        
        // Log response body if present
        try {
            if (response.promisesBody()) {
                val source = response.body?.source()
                source?.request(Long.MAX_VALUE)
                val buffer = source?.buffer
                val contentType = response.body?.contentType()
                val charset = contentType?.charset(Charset.forName("UTF-8")) 
                    ?: Charset.forName("UTF-8")
                val bodyString = buffer?.clone()?.readString(charset)
                if (bodyString != null && bodyString.isNotEmpty()) {
                    AppLogger.log(AppLogger.LogLevel.DEBUG, "Network", "Response body: ${bodyString.take(200)}...")
                }
            }
        } catch (t: Exception) {
            // Cannot read response body
        }

        return response
    }
}

