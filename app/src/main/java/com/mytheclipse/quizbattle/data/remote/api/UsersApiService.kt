package com.mytheclipse.quizbattle.data.remote.api

import com.mytheclipse.quizbattle.data.remote.model.ApiResponse
import com.mytheclipse.quizbattle.data.remote.model.UserResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface UsersApiService {
    
    @GET("api/users")
    suspend fun getAllUsers(): AllUsersResponse
    
    @GET("api/users/{id}")
    suspend fun getUserById(@Path("id") userId: String): UserDetailResponse

    @retrofit2.http.Multipart
    @retrofit2.http.POST("api/users/avatar")
    suspend fun uploadAvatar(@retrofit2.http.Part file: okhttp3.MultipartBody.Part): UploadAvatarResponse
}

data class AllUsersResponse(
    val success: Boolean,
    val count: Int,
    val users: List<UserResponse>
)

data class UserDetailResponse(
    val success: Boolean,
    val user: UserResponse
)
