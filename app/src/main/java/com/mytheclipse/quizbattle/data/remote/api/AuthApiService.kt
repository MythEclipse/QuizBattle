package com.mytheclipse.quizbattle.data.remote.api

import com.mytheclipse.quizbattle.data.remote.model.ApiResponse
import com.mytheclipse.quizbattle.data.remote.model.AuthResponseData
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    
    @POST("api/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): ApiResponse<AuthResponseData>
    
    @POST("api/auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): ApiResponse<AuthResponseData>
    
    @POST("api/auth/refresh")
    suspend fun refreshToken(): ApiResponse<String>

    @POST("api/auth/reset-password")
    suspend fun resetPassword(
        @Body request: ResetPasswordRequest
    ): ApiResponse<String>
}

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)

data class ResetPasswordRequest(
    val email: String
)
