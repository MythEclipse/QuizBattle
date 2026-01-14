package com.mytheclipse.quizbattle.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mytheclipse.quizbattle.data.repository.DataModels
import com.mytheclipse.quizbattle.data.repository.SocialMediaRepository
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI State for Social Media screen
 */
data class SocialMediaState(
    val posts: List<DataModels.Post> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val postCreated: Boolean = false
) {
    /** Check if there are any posts */
    val hasPosts: Boolean get() = posts.isNotEmpty()
    
    /** Get total posts count */
    val postsCount: Int get() = posts.size
    
    /** Get posts sorted by most recent */
    val recentPosts: List<DataModels.Post>
        get() = posts.sortedByDescending { it.createdAt }
    
    /** Get most liked posts */
    val popularPosts: List<DataModels.Post>
        get() = posts.sortedByDescending { it.likesCount }
}

/**
 * One-time events for Social Media screen
 */
sealed class SocialMediaEvent {
    data class PostCreated(val postId: String) : SocialMediaEvent()
    data class PostDeleted(val postId: String) : SocialMediaEvent()
    data class PostLiked(val postId: String) : SocialMediaEvent()
    data class CommentAdded(val postId: String) : SocialMediaEvent()
    data class ShowError(val message: String) : SocialMediaEvent()
}

/**
 * ViewModel for Social Media functionality
 */
class SocialMediaViewModel(application: Application) : AndroidViewModel(application) {
    
    // region Dependencies
    private val repository = SocialMediaRepository()
    // endregion
    
    // region State
    private val _state = MutableStateFlow(SocialMediaState())
    val state: StateFlow<SocialMediaState> = _state.asStateFlow()
    
    private val _events = MutableSharedFlow<SocialMediaEvent>()
    val events: SharedFlow<SocialMediaEvent> = _events.asSharedFlow()
    // endregion
    
    // region Exception Handler
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        logError("Coroutine error", throwable)
        updateState { copy(isLoading = false, error = throwable.message) }
    }
    // endregion
    
    init {
        loadPosts()
    }
    
    // region Public Actions
    
    fun loadPosts() {
        launchSafely {
            setLoading(true)
            
            repository.getPosts()
                .onSuccess { postResponses ->
                    val posts = postResponses.map { it.toPost() }
                    updateState { copy(posts = posts, isLoading = false) }
                }
                .onFailure { error ->
                    handleError(error.message ?: ERROR_LOAD_POSTS)
                }
        }
    }
    
    fun createPost(content: String, imageUrl: String? = null) {
        if (content.isBlank()) return
        
        launchSafely {
            setLoading(true)
            
            repository.createPost(content, imageUrl)
                .onSuccess { post ->
                    updateState { copy(isLoading = false, postCreated = true) }
                    emitEvent(SocialMediaEvent.PostCreated(post.id))
                    loadPosts() // Refresh list
                }
                .onFailure { error ->
                    handleError(error.message ?: ERROR_CREATE_POST)
                }
        }
    }
    
    fun likePost(postId: String) {
        launchSafely {
            repository.likePost(postId)
                .onSuccess {
                    emitEvent(SocialMediaEvent.PostLiked(postId))
                    loadPosts()
                }
                .onFailure { error ->
                    handleError(error.message ?: ERROR_LIKE_POST)
                }
        }
    }
    
    fun unlikePost(postId: String) {
        launchSafely {
            repository.unlikePost(postId)
                .onSuccess { loadPosts() }
                .onFailure { error ->
                    handleError(error.message ?: ERROR_UNLIKE_POST)
                }
        }
    }
    
    fun addComment(postId: String, content: String) {
        if (content.isBlank()) return
        
        launchSafely {
            repository.addComment(postId, content)
                .onSuccess {
                    emitEvent(SocialMediaEvent.CommentAdded(postId))
                    loadPosts()
                }
                .onFailure { error ->
                    handleError(error.message ?: ERROR_ADD_COMMENT)
                }
        }
    }
    
    fun deletePost(postId: String) {
        launchSafely {
            repository.deletePost(postId)
                .onSuccess {
                    updateState { 
                        copy(posts = posts.filter { it.postId != postId })
                    }
                    emitEvent(SocialMediaEvent.PostDeleted(postId))
                }
                .onFailure { error ->
                    handleError(error.message ?: ERROR_DELETE_POST)
                }
        }
    }
    
    fun resetPostCreated() {
        updateState { copy(postCreated = false) }
    }
    
    fun refresh() {
        loadPosts()
    }
    
    fun clearError() {
        updateState { copy(error = null) }
    }
    
    // endregion
    
    // region Extension Functions
    
    private fun com.mytheclipse.quizbattle.data.repository.PostResponse.toPost() = DataModels.Post(
        postId = id,
        userId = userId,
        userName = user.name ?: UNKNOWN_USER,
        content = content,
        imageUrl = imageUrl,
        likesCount = likes.size,
        commentsCount = comments.size,
        isLikedByUser = false,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )
    
    // endregion
    
    // region Utility Methods
    
    private inline fun updateState(update: SocialMediaState.() -> SocialMediaState) {
        _state.update { it.update() }
    }
    
    private fun setLoading(isLoading: Boolean) {
        updateState { copy(isLoading = isLoading, error = null) }
    }
    
    private fun handleError(message: String) {
        updateState { copy(isLoading = false, error = message) }
        emitEvent(SocialMediaEvent.ShowError(message))
    }
    
    private fun launchSafely(block: suspend () -> Unit) {
        viewModelScope.launch(exceptionHandler) { block() }
    }
    
    private fun emitEvent(event: SocialMediaEvent) {
        viewModelScope.launch { _events.emit(event) }
    }
    
    private fun logError(message: String, throwable: Throwable? = null) {
        Log.e(TAG, message, throwable)
    }
    
    // endregion
    
    companion object {
        private const val TAG = "SocialMediaViewModel"
        private const val UNKNOWN_USER = "Unknown"
        
        // Error messages
        private const val ERROR_LOAD_POSTS = "Failed to load posts"
        private const val ERROR_CREATE_POST = "Failed to create post"
        private const val ERROR_LIKE_POST = "Failed to like post"
        private const val ERROR_UNLIKE_POST = "Failed to unlike post"
        private const val ERROR_ADD_COMMENT = "Failed to add comment"
        private const val ERROR_DELETE_POST = "Failed to delete post"
    }
}
