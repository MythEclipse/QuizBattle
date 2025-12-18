package com.mytheclipse.quizbattle.data.remote

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.internal.http.promisesBody
import okio.Buffer
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * ApiLoggingInterceptor
 * Logs request and response metadata in a structured and detailed way.
 * Only active in DEBUG builds.
 */
class ApiLoggingInterceptor(private val tag: String = "API") : Interceptor {
    
    private val dateFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val requestStart = System.nanoTime()
        val timestamp = dateFormat.format(Date())

        val requestBody = request.body
        val hasRequestBody = requestBody != null

        // Log request separator and basic info
        Log.d(tag, "╔══════════════════════════════════════════════════════════════")
        Log.d(tag, "║ [$timestamp] REQUEST: ${request.method} ${request.url}")
        Log.d(tag, "╠══════════════════════════════════════════════════════════════")

        // Log headers but mask Authorization
        val headers = request.headers
        if (headers.size > 0) {
            Log.d(tag, "║ HEADERS:")
            for (i in 0 until headers.size) {
                val name = headers.name(i)
                var value = headers.value(i)
                if (name.equals("Authorization", ignoreCase = true) && value.isNotBlank()) {
                    value = value.replaceAfter(' ', "****")
                }
                Log.d(tag, "║   $name: $value")
            }
        }

        // Log request body
        if (hasRequestBody) {
            val contentLength = requestBody?.contentLength() ?: -1
            Log.d(tag, "║ BODY: (${if (contentLength >= 0) "${contentLength} bytes" else "unknown size"})")
            try {
                val buffer = Buffer()
                requestBody?.writeTo(buffer)
                var charset: Charset = Charset.forName("UTF-8")
                val contentType = requestBody?.contentType()
                if (contentType != null) {
                    contentType.charset(Charset.forName("UTF-8"))?.let { charset = it }
                }
                val body = buffer.readString(charset)
                // Pretty print JSON if it looks like JSON
                val prettyBody = if (body.startsWith("{") || body.startsWith("[")) {
                    body.chunked(100).joinToString("\n║     ")
                } else {
                    body
                }
                Log.d(tag, "║   $prettyBody")
            } catch (e: Exception) {
                Log.d(tag, "║   <could not read body>")
            }
        } else {
            Log.d(tag, "║ BODY: (empty)")
        }

        Log.d(tag, "╠══════════════════════════════════════════════════════════════")

        // Execute request
        val response: Response
        try {
            response = chain.proceed(request)
        } catch (e: Exception) {
            Log.e(tag, "║ ❌ HTTP FAILED: ${e.message}")
            Log.e(tag, "╚══════════════════════════════════════════════════════════════")
            throw e
        }

        val tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - requestStart)
        val responseTimestamp = dateFormat.format(Date())
        
        // Response status icon
        val statusIcon = when {
            response.code in 200..299 -> "✅"
            response.code in 300..399 -> "↪️"
            response.code in 400..499 -> "⚠️"
            response.code >= 500 -> "❌"
            else -> "❓"
        }

        Log.d(tag, "║ [$responseTimestamp] RESPONSE: $statusIcon ${response.code} ${response.message} (${tookMs}ms)")
        Log.d(tag, "╠══════════════════════════════════════════════════════════════")

        // Log response headers
        val responseHeaders = response.headers
        if (responseHeaders.size > 0) {
            Log.d(tag, "║ HEADERS:")
            for (i in 0 until responseHeaders.size) {
                Log.d(tag, "║   ${responseHeaders.name(i)}: ${responseHeaders.value(i)}")
            }
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
                val contentLength = response.body?.contentLength() ?: -1
                Log.d(tag, "║ BODY: (${if (contentLength >= 0) "${contentLength} bytes" else "unknown size"})")
                val bodyString = buffer?.clone()?.readString(charset)
                if (bodyString != null && bodyString.isNotEmpty()) {
                    // Pretty print JSON if it looks like JSON
                    val prettyBody = if (bodyString.startsWith("{") || bodyString.startsWith("[")) {
                        bodyString.chunked(100).joinToString("\n║     ")
                    } else {
                        bodyString
                    }
                    Log.d(tag, "║   $prettyBody")
                } else {
                    Log.d(tag, "║   <empty>")
                }
            }
        } catch (t: Exception) {
            Log.d(tag, "║ BODY: <could not read>")
        }

        Log.d(tag, "╚══════════════════════════════════════════════════════════════")

        return response
    }
}

