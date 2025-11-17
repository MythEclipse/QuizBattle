package com.mytheclipse.quizbattle.data.remote

import com.google.gson.GsonBuilder
import android.util.Log
import com.mytheclipse.quizbattle.BuildConfig
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

    // Custom API logging. Wrap with BuildConfig debug so it won't flood release builds
    private val apiLoggingInterceptor = ApiLoggingInterceptor("API")
    
    private val errorHandlerInterceptor = ErrorHandlerInterceptor()
    
    private val okHttpClientBuilder = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .addInterceptor(errorHandlerInterceptor)

    // Add more verbose API logging only for debug builds
    private val okHttpClient = okHttpClientBuilder.apply {
        if (BuildConfig.DEBUG) {
            addInterceptor(apiLoggingInterceptor)
        }
    }.build()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
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
