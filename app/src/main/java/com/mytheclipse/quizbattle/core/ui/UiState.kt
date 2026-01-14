package com.mytheclipse.quizbattle.core.ui

/**
 * Represents UI state for data loading operations
 * Follows the single source of truth pattern
 * 
 * @param T The type of data this state holds
 */
sealed class UiState<out T> {
    
    /**
     * Initial empty state before any operation
     */
    data object Empty : UiState<Nothing>()
    
    /**
     * Loading state - operation in progress
     */
    data object Loading : UiState<Nothing>()
    
    /**
     * Success state with data
     * @param data The loaded data
     */
    data class Success<T>(val data: T) : UiState<T>()
    
    /**
     * Error state with message
     * @param message Error description
     * @param throwable Optional exception for debugging
     */
    data class Error(
        val message: String,
        val throwable: Throwable? = null
    ) : UiState<Nothing>()

    // ===== Utility Properties =====
    
    val isLoading: Boolean get() = this is Loading
    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error
    val isEmpty: Boolean get() = this is Empty

    /**
     * Get data if Success, null otherwise
     */
    fun getOrNull(): T? = (this as? Success)?.data

    /**
     * Get error message if Error, null otherwise
     */
    fun errorOrNull(): String? = (this as? Error)?.message
}

/**
 * Map data in Success state to another type
 */
inline fun <T, R> UiState<T>.map(transform: (T) -> R): UiState<R> {
    return when (this) {
        is UiState.Empty -> UiState.Empty
        is UiState.Loading -> UiState.Loading
        is UiState.Success -> UiState.Success(transform(data))
        is UiState.Error -> UiState.Error(message, throwable)
    }
}

/**
 * Execute action only when Success
 */
inline fun <T> UiState<T>.onSuccess(action: (T) -> Unit): UiState<T> {
    if (this is UiState.Success) action(data)
    return this
}

/**
 * Execute action only when Error
 */
inline fun <T> UiState<T>.onError(action: (String) -> Unit): UiState<T> {
    if (this is UiState.Error) action(message)
    return this
}

/**
 * Execute action only when Loading
 */
inline fun <T> UiState<T>.onLoading(action: () -> Unit): UiState<T> {
    if (this is UiState.Loading) action()
    return this
}
