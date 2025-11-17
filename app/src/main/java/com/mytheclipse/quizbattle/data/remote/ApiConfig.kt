package com.mytheclipse.quizbattle.data.remote

import com.google.gson.GsonBuilder
import android.util.Log
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiConfig {
    private const val BASE_URL = "https://elysia.asepharyana.tech/"
    const val WEBSOCKET_URL = "wss://elysia.asepharyana.tech/api/quiz/battle"
    
    private var authToken: String? = null
    
    fun setAuthToken(token: String?) {
        authToken = token
    }
    
    private val authInterceptor = Interceptor { chain ->
        val requestBuilder = chain.request().newBuilder()
        authToken?.let {
            requestBuilder.addHeader("Authorization", "Bearer $it")
        }
        chain.proceed(requestBuilder.build())
    }
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Custom API logging. Wrap with debug detection so it won't flood release builds
    private val apiLoggingInterceptor = ApiLoggingInterceptor("API")
    
    private val errorHandlerInterceptor = ErrorHandlerInterceptor()
    
    // Use reflection to avoid compile-time dependency on BuildConfig in edge cases
    private val isDebug: Boolean = try {
        val buildConfigClass = Class.forName("com.mytheclipse.quizbattle.BuildConfig")
        val field = buildConfigClass.getDeclaredField("DEBUG")
        field.isAccessible = true
        field.getBoolean(null)
    } catch (e: Exception) {
        false
    }

    private val okHttpClientBuilder = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .addInterceptor(errorHandlerInterceptor)

    // Add more verbose API logging only for debug builds
    init {
        if (isDebug) {
            okHttpClientBuilder.addInterceptor(apiLoggingInterceptor)
        }
    }

    private val okHttpClient = okHttpClientBuilder.build()
    
    private val gson = GsonBuilder()
        .setLenient()
        .create()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
    
    fun <T> createService(serviceClass: Class<T>): T {
        return retrofit.create(serviceClass)
    }
}
