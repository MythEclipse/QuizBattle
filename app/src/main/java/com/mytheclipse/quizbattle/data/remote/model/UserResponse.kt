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

data class AuthResponseData(
    @SerializedName("user")
    val user: UserResponse,
    
    @SerializedName("token")
    val token: String
)
