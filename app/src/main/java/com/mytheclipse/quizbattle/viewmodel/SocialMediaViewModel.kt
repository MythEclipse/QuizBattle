package com.mytheclipse.quizbattle.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mytheclipse.quizbattle.data.repository.DataModels
import com.mytheclipse.quizbattle.data.repository.SocialMediaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SocialMediaState(
    val posts: List<DataModels.Post> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val postCreated: Boolean = false
)

class SocialMediaViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = SocialMediaRepository()
    
    private val _state = MutableStateFlow(SocialMediaState())
    val state: StateFlow<SocialMediaState> = _state.asStateFlow()
    
    init {
        loadPosts()
    }
    
    fun loadPosts() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            repository.getPosts().onSuccess { postResponses ->
                val posts = postResponses.map { p ->
                    DataModels.Post(
                        postId = p.id,
                        userId = p.userId,
                        userName = p.user.name ?: "Unknown",
                        content = p.content,
                        imageUrl = p.imageUrl,
                        likesCount = p.likes.size,
                        commentsCount = p.comments.size,
                        isLikedByUser = false,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                }
                _state.value = _state.value.copy(
                    posts = posts,
                    isLoading = false
                )
            }.onFailure { error ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = error.message ?: "Failed to load posts"
                )
            }
        }
    }
    
    fun createPost(content: String, imageUrl: String? = null) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            repository.createPost(content, imageUrl).onSuccess { post ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    postCreated = true
                )
                loadPosts()
            }.onFailure { error ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = error.message ?: "Failed to create post"
                )
            }
        }
    }
    
    fun likePost(postId: String) {
        viewModelScope.launch {
            repository.likePost(postId).onSuccess {
                loadPosts() // Refresh posts
            }
        }
    }
    
    fun unlikePost(postId: String) {
        viewModelScope.launch {
            repository.unlikePost(postId).onSuccess {
                loadPosts() // Refresh posts
            }
        }
    }
    
    fun addComment(postId: String, content: String) {
        viewModelScope.launch {
            repository.addComment(postId, content).onSuccess {
                loadPosts() // Refresh posts
            }
        }
    }
    
    fun deletePost(postId: String) {
        viewModelScope.launch {
            repository.deletePost(postId).onSuccess {
                _state.value = _state.value.copy(
                    posts = _state.value.posts.filter { it.postId != postId }
                )
            }
        }
    }
    
    fun resetPostCreated() {
        _state.value = _state.value.copy(postCreated = false)
    }
}
