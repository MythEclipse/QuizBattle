package com.mytheclipse.quizbattle.data.remote

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.internal.http.promisesBody
import okio.Buffer
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit

/**
 * ApiLoggingInterceptor
 * Logs request and response metadata in a structured and masked way.
 */
class ApiLoggingInterceptor(private val tag: String = "API") : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val requestStart = System.nanoTime()

        val requestBody = request.body
        val hasRequestBody = requestBody != null

        // Log request line
        val requestLine = "--> ${request.method} ${request.url}"
        Log.d(tag, requestLine)

        // Log headers but mask Authorization
        val headers = request.headers
        for (i in 0 until headers.size) {
            val name = headers.name(i)
            var value = headers.value(i)
            if (name.equals("Authorization", ignoreCase = true) && value.isNotBlank()) {
                value = value.replaceAfter(' ', "****")
            }
            Log.d(tag, "$name: $value")
        }

        if (hasRequestBody) {
            try {
                val buffer = Buffer()
                requestBody?.writeTo(buffer)
                var charset: Charset = Charset.forName("UTF-8")
                val contentType = requestBody?.contentType()
                if (contentType != null) {
                    contentType.charset(Charset.forName("UTF-8"))?.let { charset = it }
                }
                val body = buffer.readString(charset)
                Log.d(tag, "Request Body: $body")
            } catch (e: Exception) {
                Log.d(tag, "Request Body: <could not read>")
            }
        }

        val response: Response
        try {
            response = chain.proceed(request)
        } catch (e: Exception) {
            Log.e(tag, "<-- HTTP FAILED: ${e.message}")
            throw e
        }

        val tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - requestStart)
        val responseLine = "<-- ${response.code} ${response.request.url} (${tookMs}ms)"
        Log.d(tag, responseLine)

        // Log response headers
        val responseHeaders = response.headers
        for (i in 0 until responseHeaders.size) {
            Log.d(tag, "${responseHeaders.name(i)}: ${responseHeaders.value(i)}")
        }

        // Log response body if possible
        try {
            if (response.promisesBody()) {
                val source = response.body?.source()
                source?.request(Long.MAX_VALUE)
                val buffer = source?.buffer
                val contentType = response.body?.contentType()
                var charset: Charset = Charset.forName("UTF-8")
                if (contentType != null) {
                    contentType.charset(Charset.forName("UTF-8"))?.let { charset = it }
                }
                val bodyString = buffer?.clone()?.readString(charset)
                Log.d(tag, "Response Body: ${bodyString ?: "<empty>"}")
            }
        } catch (t: Exception) {
            Log.d(tag, "Response Body: <could not read>")
        }

        return response
    }
}
