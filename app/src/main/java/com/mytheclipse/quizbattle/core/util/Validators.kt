package com.mytheclipse.quizbattle.core.util

/**
 * Validation result wrapper
 */
sealed class ValidationResult {
    data object Valid : ValidationResult()
    data class Invalid(val message: String) : ValidationResult()
    
    val isValid: Boolean get() = this is Valid
    val errorMessage: String? get() = (this as? Invalid)?.message
}

/**
 * Centralized validation utilities
 */
object Validators {
    
    // ===== Email Validation =====
    
    fun validateEmail(email: String): ValidationResult {
        return when {
            email.isBlank() -> ValidationResult.Invalid("Email tidak boleh kosong")
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> 
                ValidationResult.Invalid("Format email tidak valid")
            else -> ValidationResult.Valid
        }
    }
    
    // ===== Password Validation =====
    
    fun validatePassword(password: String): ValidationResult {
        return when {
            password.isBlank() -> ValidationResult.Invalid("Password tidak boleh kosong")
            password.length < 8 -> ValidationResult.Invalid("Password minimal 8 karakter")
            !password.any { it.isUpperCase() } -> 
                ValidationResult.Invalid("Password harus mengandung huruf besar")
            !password.any { it.isLowerCase() } -> 
                ValidationResult.Invalid("Password harus mengandung huruf kecil")
            !password.any { it.isDigit() } -> 
                ValidationResult.Invalid("Password harus mengandung angka")
            !password.any { !it.isLetterOrDigit() } -> 
                ValidationResult.Invalid("Password harus mengandung simbol")
            else -> ValidationResult.Valid
        }
    }
    
    /**
     * Simple password validation (minimum length only)
     */
    fun validateSimplePassword(password: String, minLength: Int = 6): ValidationResult {
        return when {
            password.isBlank() -> ValidationResult.Invalid("Password tidak boleh kosong")
            password.length < minLength -> 
                ValidationResult.Invalid("Password minimal $minLength karakter")
            else -> ValidationResult.Valid
        }
    }
    
    fun validatePasswordMatch(password: String, confirmPassword: String): ValidationResult {
        return if (password == confirmPassword) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid("Password tidak cocok")
        }
    }
    
    // ===== Username Validation =====
    
    fun validateUsername(username: String): ValidationResult {
        return when {
            username.isBlank() -> ValidationResult.Invalid("Username tidak boleh kosong")
            username.length < 3 -> ValidationResult.Invalid("Username minimal 3 karakter")
            username.length > 20 -> ValidationResult.Invalid("Username maksimal 20 karakter")
            !username.matches(Regex("^[a-zA-Z0-9_]+$")) -> 
                ValidationResult.Invalid("Username hanya boleh huruf, angka, dan underscore")
            else -> ValidationResult.Valid
        }
    }
    
    // ===== General Validation =====
    
    fun validateNotEmpty(value: String, fieldName: String): ValidationResult {
        return if (value.isNotBlank()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid("$fieldName tidak boleh kosong")
        }
    }
    
    fun validateMinLength(value: String, minLength: Int, fieldName: String): ValidationResult {
        return if (value.length >= minLength) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid("$fieldName minimal $minLength karakter")
        }
    }
    
    fun validateMaxLength(value: String, maxLength: Int, fieldName: String): ValidationResult {
        return if (value.length <= maxLength) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid("$fieldName maksimal $maxLength karakter")
        }
    }
    
    fun validateRange(value: Int, min: Int, max: Int, fieldName: String): ValidationResult {
        return if (value in min..max) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid("$fieldName harus antara $min dan $max")
        }
    }
    
    // ===== Combine Multiple Validations =====
    
    /**
     * Run multiple validations and return first error if any
     */
    fun validateAll(vararg validations: () -> ValidationResult): ValidationResult {
        validations.forEach { validation ->
            val result = validation()
            if (result is ValidationResult.Invalid) {
                return result
            }
        }
        return ValidationResult.Valid
    }
    
    /**
     * Run multiple validations and collect all errors
     */
    fun validateAllErrors(vararg validations: () -> ValidationResult): List<String> {
        return validations.mapNotNull { validation ->
            val result = validation()
            (result as? ValidationResult.Invalid)?.message
        }
    }
}

/**
 * Extension to validate and get result with callback
 */
inline fun ValidationResult.onInvalid(action: (String) -> Unit): ValidationResult {
    if (this is ValidationResult.Invalid) {
        action(message)
    }
    return this
}

inline fun ValidationResult.onValid(action: () -> Unit): ValidationResult {
    if (this is ValidationResult.Valid) {
        action()
    }
    return this
}
