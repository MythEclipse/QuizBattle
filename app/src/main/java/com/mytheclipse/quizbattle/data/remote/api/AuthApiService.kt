package com.mytheclipse.quizbattle.data.remote.api

import com.mytheclipse.quizbattle.data.remote.model.ApiResponse
import com.mytheclipse.quizbattle.data.remote.model.LoginResponseData
import com.mytheclipse.quizbattle.data.remote.model.RegisterResponseData
import com.mytheclipse.quizbattle.data.remote.model.RefreshTokenResponseData
import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.POST

data class ResetPasswordRequest(
    val token: String,
    @SerializedName("new_password")
    val newPassword: String
)

data class GoogleLoginRequest(
    val idToken: String
)

interface AuthApiService {
    
    @POST("api/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): LoginResponseData
    
    @POST("api/auth/google")
    suspend fun googleLogin(
        @Body request: GoogleLoginRequest
    ): LoginResponseData
    
    @POST("api/auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): RegisterResponseData
    
    @POST("api/auth/refresh-token")
    suspend fun refreshToken(
        @Body request: RefreshTokenRequest
    ): RefreshTokenResponseData

    @POST("api/auth/forgot-password")
    suspend fun forgotPassword(
        @Body request: ForgotPasswordRequest
    ): ApiResponse<String>
    
    @POST("api/auth/reset-password")
    suspend fun resetPassword(
        @Body request: ResetPasswordRequest
    ): ApiResponse<String>
}

data class LoginRequest(
    val email: String,
    val password: String,
    val rememberMe: Boolean = false
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)

data class RefreshTokenRequest(
    @SerializedName("refresh_token")
    val refreshToken: String
)

data class ForgotPasswordRequest(
    val email: String
)

