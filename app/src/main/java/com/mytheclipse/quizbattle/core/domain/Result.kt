package com.mytheclipse.quizbattle.core.domain

/**
 * Generic Result wrapper for domain layer operations
 * Replaces exceptions with explicit error handling
 */
sealed class Result<out T> {
    
    /**
     * Successful result with data
     */
    data class Success<T>(val data: T) : Result<T>()
    
    /**
     * Failed result with error
     */
    data class Failure(
        val message: String,
        val exception: Throwable? = null,
        val code: Int? = null
    ) : Result<Nothing>()

    // ===== Utility Properties =====
    
    val isSuccess: Boolean get() = this is Success
    val isFailure: Boolean get() = this is Failure

    /**
     * Get data if Success, null otherwise
     */
    fun getOrNull(): T? = (this as? Success)?.data

    /**
     * Get data if Success, throw exception otherwise
     */
    fun getOrThrow(): T {
        return when (this) {
            is Success -> data
            is Failure -> throw exception ?: Exception(message)
        }
    }

    /**
     * Get data if Success, default value otherwise
     */
    fun getOrDefault(default: @UnsafeVariance T): T {
        return (this as? Success)?.data ?: default
    }

    /**
     * Get error message if Failure, null otherwise
     */
    fun exceptionOrNull(): Throwable? = (this as? Failure)?.exception
    
    fun messageOrNull(): String? = (this as? Failure)?.message

    companion object {
        /**
         * Create a Success result
         */
        fun <T> success(data: T): Result<T> = Success(data)

        /**
         * Create a Failure result from message
         */
        fun failure(message: String, code: Int? = null): Result<Nothing> = 
            Failure(message, null, code)

        /**
         * Create a Failure result from exception
         */
        fun failure(exception: Throwable): Result<Nothing> = 
            Failure(exception.message ?: "Unknown error", exception)

        /**
         * Wrap a block in Result, catching exceptions
         */
        inline fun <T> runCatching(block: () -> T): Result<T> {
            return try {
                Success(block())
            } catch (e: Exception) {
                Failure(e.message ?: "Unknown error", e)
            }
        }
    }
}

/**
 * Map data in Success to another type
 */
inline fun <T, R> Result<T>.map(transform: (T) -> R): Result<R> {
    return when (this) {
        is Result.Success -> Result.Success(transform(data))
        is Result.Failure -> this
    }
}

/**
 * Map data in Success to another Result
 */
inline fun <T, R> Result<T>.flatMap(transform: (T) -> Result<R>): Result<R> {
    return when (this) {
        is Result.Success -> transform(data)
        is Result.Failure -> this
    }
}

/**
 * Execute action only when Success
 */
inline fun <T> Result<T>.onSuccess(action: (T) -> Unit): Result<T> {
    if (this is Result.Success) action(data)
    return this
}

/**
 * Execute action only when Failure
 */
inline fun <T> Result<T>.onFailure(action: (String) -> Unit): Result<T> {
    if (this is Result.Failure) action(message)
    return this
}

/**
 * Recover from failure with a default value
 */
inline fun <T> Result<T>.recover(transform: (Result.Failure) -> T): Result<T> {
    return when (this) {
        is Result.Success -> this
        is Result.Failure -> Result.Success(transform(this))
    }
}

/**
 * Convert Result to UiState
 */
fun <T> Result<T>.toUiState(): com.mytheclipse.quizbattle.core.ui.UiState<T> {
    return when (this) {
        is Result.Success -> com.mytheclipse.quizbattle.core.ui.UiState.Success(data)
        is Result.Failure -> com.mytheclipse.quizbattle.core.ui.UiState.Error(message, exception)
    }
}
