package com.mytheclipse.quizbattle.data.model

/**
 * Wrapper class for API responses with loading states
 * Represents three states: Loading, Success, and Error
 */
sealed class Resource<out T> {
    /**
     * Loading state - operation in progress
     */
    object Loading : Resource<Nothing>()
    
    /**
     * Success state - operation completed successfully
     * @param data The result data
     */
    data class Success<T>(val data: T) : Resource<T>()
    
    /**
     * Error state - operation failed
     * @param message Error message
     * @param throwable Optional throwable for detailed error info
     */
    data class Error(
        val message: String,
        val throwable: Throwable? = null
    ) : Resource<Nothing>()
}

/**
 * Extension functions for Resource handling
 */

/**
 * Returns data if Success, null otherwise
 */
fun <T> Resource<T>.getDataOrNull(): T? {
    return when (this) {
        is Resource.Success -> data
        else -> null
    }
}

/**
 * Returns true if this is a Success state
 */
fun <T> Resource<T>.isSuccess(): Boolean {
    return this is Resource.Success
}

/**
 * Returns true if this is an Error state
 */
fun <T> Resource<T>.isError(): Boolean {
    return this is Resource.Error
}

/**
 * Returns true if this is a Loading state
 */
fun <T> Resource<T>.isLoading(): Boolean {
    return this is Resource.Loading
}

/**
 * Execute block if Success
 */
inline fun <T> Resource<T>.onSuccess(block: (T) -> Unit): Resource<T> {
    if (this is Resource.Success) {
        block(data)
    }
    return this
}

/**
 * Execute block if Error
 */
inline fun <T> Resource<T>.onError(block: (String, Throwable?) -> Unit): Resource<T> {
    if (this is Resource.Error) {
        block(message, throwable)
    }
    return this
}

/**
 * Execute block if Loading
 */
inline fun <T> Resource<T>.onLoading(block: () -> Unit): Resource<T> {
    if (this is Resource.Loading) {
        block()
    }
    return this
}

/**
 * Map the data if Success
 */
inline fun <T, R> Resource<T>.map(transform: (T) -> R): Resource<R> {
    return when (this) {
        is Resource.Success -> Resource.Success(transform(data))
        is Resource.Error -> Resource.Error(message, throwable)
        is Resource.Loading -> Resource.Loading
    }
}
