package com.mytheclipse.quizbattle.data.repository

import android.util.Log
import com.mytheclipse.quizbattle.data.remote.ApiConfig
import com.mytheclipse.quizbattle.BuildConfig
import com.mytheclipse.quizbattle.data.remote.api.*
import com.mytheclipse.quizbattle.data.remote.model.CommentResponse
import com.mytheclipse.quizbattle.data.remote.model.LikeResponse
import com.mytheclipse.quizbattle.data.remote.model.PostResponse

class SocialMediaRepository {
    
    private val apiService = ApiConfig.createService(SocialMediaApiService::class.java)
    
    suspend fun getPosts(): Result<List<PostResponse>> {
        return try {
            if (BuildConfig.DEBUG) Log.d("API", "SocialMediaRepository.getPosts - start")
            val response = apiService.getPosts()
            if (response.success) {
                if (BuildConfig.DEBUG) Log.d("API", "SocialMediaRepository.getPosts - success count=${response.posts.size}")
                Result.success(response.posts)
            } else {
                if (BuildConfig.DEBUG) Log.e("API", "SocialMediaRepository.getPosts - failed: ${response}")
                Result.failure(Exception("Failed to fetch posts"))
            }
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) Log.e("API", "SocialMediaRepository.getPosts - exception: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun createPost(content: String, imageUrl: String? = null): Result<PostResponse> {
        return try {
            if (BuildConfig.DEBUG) Log.d("API", "SocialMediaRepository.createPost - start contentLen=${content.length}")
            val response = apiService.createPost(CreatePostRequest(content, imageUrl))
            if (response.success) {
                if (BuildConfig.DEBUG) Log.d("API", "SocialMediaRepository.createPost - success postId=${response.post.id}")
                Result.success(response.post)
            } else {
                if (BuildConfig.DEBUG) Log.e("API", "SocialMediaRepository.createPost - failed: ${response}")
                Result.failure(Exception("Failed to create post"))
            }
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) Log.e("API", "SocialMediaRepository.createPost - exception: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun updatePost(postId: String, content: String, imageUrl: String? = null): Result<PostResponse> {
        return try {
            val response = apiService.updatePost(postId, UpdatePostRequest(content, imageUrl))
            if (response.success) {
                Result.success(response.post)
            } else {
                Result.failure(Exception("Failed to update post"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deletePost(postId: String): Result<Boolean> {
        return try {
            val response = apiService.deletePost(postId)
            if (response.success) {
                Result.success(true)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun addComment(postId: String, content: String): Result<CommentResponse> {
        return try {
            val response = apiService.addComment(postId, AddCommentRequest(content))
            if (response.success) {
                Result.success(response.comment)
            } else {
                Result.failure(Exception("Failed to add comment"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateComment(commentId: String, content: String): Result<CommentResponse> {
        return try {
            val response = apiService.updateComment(commentId, UpdateCommentRequest(content))
            if (response.success) {
                Result.success(response.comment)
            } else {
                Result.failure(Exception("Failed to update comment"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteComment(commentId: String): Result<Boolean> {
        return try {
            val response = apiService.deleteComment(commentId)
            if (response.success) {
                Result.success(true)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun likePost(postId: String): Result<LikeResponse> {
        return try {
            val response = apiService.likePost(postId)
            if (response.success) {
                Result.success(response.like)
            } else {
                Result.failure(Exception("Failed to like post"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun unlikePost(postId: String): Result<Boolean> {
        return try {
            val response = apiService.unlikePost(postId)
            if (response.success) {
                Result.success(true)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
