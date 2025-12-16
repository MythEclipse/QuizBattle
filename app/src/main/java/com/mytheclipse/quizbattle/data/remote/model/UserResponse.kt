package com.mytheclipse.quizbattle.data.remote.model

import com.google.gson.annotations.SerializedName

data class UserResponse(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("email")
    val email: String?,
    
    @SerializedName("name")
    val name: String?,
    
    @SerializedName("emailVerified")
    val emailVerified: String?,
    
    @SerializedName("image")
    val image: String?,
    
    @SerializedName("role")
    val role: String = "user"
)

// Login response - Elysia returns accessToken + refreshToken
data class LoginResponseData(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("user")
    val user: UserResponse,
    
    @SerializedName("accessToken")
    val accessToken: String,
    
    @SerializedName("refreshToken")
    val refreshToken: String,
    
    @SerializedName("tokenType")
    val tokenType: String = "Bearer",
    
    @SerializedName("expiresIn")
    val expiresIn: Int
)

// Register response - Elysia requires email verification (no token returned)
data class RegisterResponseData(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("user")
    val user: UserResponse
)

// Refresh token response
data class RefreshTokenResponseData(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("accessToken")
    val accessToken: String,
    
    @SerializedName("refreshToken")
    val refreshToken: String,
    
    @SerializedName("tokenType")
    val tokenType: String = "Bearer",
    
    @SerializedName("expiresIn")
    val expiresIn: Int
)
