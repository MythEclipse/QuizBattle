package com.mytheclipse.quizbattle.data.remote

/**
 * Custom exception for API errors
 */
sealed class ApiException(message: String) : Exception(message) {
    
    /**
     * Network connection error (no internet)
     */
    class NetworkException(message: String = "No internet connection") : ApiException(message)
    
    /**
     * Timeout error
     */
    class TimeoutException(message: String = "Request timeout") : ApiException(message)
    
    /**
     * Server error (5xx)
     */
    class ServerException(val code: Int, message: String = "Server error") : ApiException(message)
    
    /**
     * Authentication error (401)
     */
    class AuthException(message: String = "Authentication failed") : ApiException(message)
    
    /**
     * Not found error (404)
     */
    class NotFoundException(message: String = "Resource not found") : ApiException(message)
    
    /**
     * Bad request error (400)
     */
    class BadRequestException(message: String = "Bad request") : ApiException(message)
    
    /**
     * Forbidden error (403)
     */
    class ForbiddenException(message: String = "Access forbidden") : ApiException(message)
    
    /**
     * Conflict error (409)
     */
    class ConflictException(message: String = "Conflict error") : ApiException(message)
    
    /**
     * Unknown/Generic error
     */
    class UnknownException(message: String = "Unknown error occurred") : ApiException(message)
}

/**
 * Extension function to convert HTTP status code to ApiException
 */
fun Int.toApiException(message: String? = null): ApiException {
    return when (this) {
        400 -> ApiException.BadRequestException(message ?: "Bad request")
        401 -> ApiException.AuthException(message ?: "Authentication failed")
        403 -> ApiException.ForbiddenException(message ?: "Access forbidden")
        404 -> ApiException.NotFoundException(message ?: "Resource not found")
        409 -> ApiException.ConflictException(message ?: "Conflict error")
        in 500..599 -> ApiException.ServerException(this, message ?: "Server error")
        else -> ApiException.UnknownException(message ?: "Unknown error")
    }
}
