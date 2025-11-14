package com.mytheclipse.quizbattle.data.remote.model

import com.google.gson.annotations.SerializedName

data class PostResponse(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("userId")
    val userId: String,
    
    @SerializedName("content")
    val content: String,
    
    @SerializedName("image_url")
    val imageUrl: String?,
    
    @SerializedName("created_at")
    val createdAt: String,
    
    @SerializedName("user")
    val user: UserResponse,
    
    @SerializedName("comments")
    val comments: List<CommentResponse> = emptyList(),
    
    @SerializedName("likes")
    val likes: List<LikeResponse> = emptyList()
)

data class CommentResponse(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("postId")
    val postId: String,
    
    @SerializedName("userId")
    val userId: String,
    
    @SerializedName("content")
    val content: String,
    
    @SerializedName("created_at")
    val createdAt: String,
    
    @SerializedName("user")
    val user: UserResponse
)

data class LikeResponse(
    @SerializedName("postId")
    val postId: String,
    
    @SerializedName("userId")
    val userId: String,
    
    @SerializedName("user")
    val user: UserResponse
)
