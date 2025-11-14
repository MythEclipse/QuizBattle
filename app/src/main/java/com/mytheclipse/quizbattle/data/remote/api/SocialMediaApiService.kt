package com.mytheclipse.quizbattle.data.remote.api

import com.mytheclipse.quizbattle.data.remote.model.ApiResponse
import com.mytheclipse.quizbattle.data.remote.model.CommentResponse
import com.mytheclipse.quizbattle.data.remote.model.LikeResponse
import com.mytheclipse.quizbattle.data.remote.model.PostResponse
import retrofit2.http.*

interface SocialMediaApiService {
    
    @GET("api/sosmed/posts")
    suspend fun getPosts(): PostsResponse
    
    @POST("api/sosmed/posts")
    suspend fun createPost(@Body request: CreatePostRequest): CreatePostResponse
    
    @PUT("api/sosmed/posts/{id}")
    suspend fun updatePost(
        @Path("id") postId: String,
        @Body request: UpdatePostRequest
    ): UpdatePostResponse
    
    @DELETE("api/sosmed/posts/{id}")
    suspend fun deletePost(@Path("id") postId: String): DeleteResponse
    
    @POST("api/sosmed/posts/{id}/comments")
    suspend fun addComment(
        @Path("id") postId: String,
        @Body request: AddCommentRequest
    ): AddCommentResponse
    
    @PUT("api/sosmed/comments/{id}")
    suspend fun updateComment(
        @Path("id") commentId: String,
        @Body request: UpdateCommentRequest
    ): UpdateCommentResponse
    
    @DELETE("api/sosmed/comments/{id}")
    suspend fun deleteComment(@Path("id") commentId: String): DeleteResponse
    
    @POST("api/sosmed/posts/{id}/like")
    suspend fun likePost(@Path("id") postId: String): LikePostResponse
    
    @DELETE("api/sosmed/posts/{id}/like")
    suspend fun unlikePost(@Path("id") postId: String): DeleteResponse
}

data class PostsResponse(
    val success: Boolean,
    val posts: List<PostResponse>
)

data class CreatePostRequest(
    val content: String,
    val imageUrl: String? = null
)

data class CreatePostResponse(
    val success: Boolean,
    val post: PostResponse
)

data class UpdatePostRequest(
    val content: String,
    val imageUrl: String? = null
)

data class UpdatePostResponse(
    val success: Boolean,
    val post: PostResponse
)

data class DeleteResponse(
    val success: Boolean,
    val message: String
)

data class AddCommentRequest(
    val content: String
)

data class AddCommentResponse(
    val success: Boolean,
    val comment: CommentResponse
)

data class UpdateCommentRequest(
    val content: String
)

data class UpdateCommentResponse(
    val success: Boolean,
    val comment: CommentResponse
)

data class LikePostResponse(
    val success: Boolean,
    val like: LikeResponse
)
