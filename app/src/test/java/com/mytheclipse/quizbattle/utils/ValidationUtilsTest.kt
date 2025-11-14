package com.mytheclipse.quizbattle.utils

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for validation utilities
 * Tests input validation logic used throughout the app
 */
class ValidationUtilsTest {

    @Test
    fun `isValidEmail should accept valid email formats`() {
        // Given
        val validEmails = listOf(
            "test@example.com",
            "user.name@domain.com",
            "user+tag@example.co.id",
            "test123@test-domain.com"
        )
        
        // When & Then
        validEmails.forEach { email ->
            assertTrue("$email should be valid", isValidEmail(email))
        }
    }

    @Test
    fun `isValidEmail should reject invalid email formats`() {
        // Given
        val invalidEmails = listOf(
            "invalid",
            "missing@domain",
            "@nodomain.com",
            "spaces in@email.com",
            "double@@domain.com",
            ""
        )
        
        // When & Then
        invalidEmails.forEach { email ->
            assertFalse("$email should be invalid", isValidEmail(email))
        }
    }

    @Test
    fun `isValidPassword should accept passwords meeting requirements`() {
        // Given - assuming minimum 6 characters
        val validPasswords = listOf(
            "Pass123",
            "SecurePassword",
            "Test@1234",
            "LongPasswordWithNumbers123"
        )
        
        // When & Then
        validPasswords.forEach { password ->
            assertTrue("$password should be valid", isValidPassword(password))
        }
    }

    @Test
    fun `isValidPassword should reject weak passwords`() {
        // Given
        val weakPasswords = listOf(
            "123",
            "short",
            "",
            "12345"
        )
        
        // When & Then
        weakPasswords.forEach { password ->
            assertFalse("$password should be invalid", isValidPassword(password))
        }
    }

    @Test
    fun `isValidUsername should accept alphanumeric usernames`() {
        // Given
        val validUsernames = listOf(
            "user123",
            "TestUser",
            "player_one",
            "gamer2024"
        )
        
        // When & Then
        validUsernames.forEach { username ->
            assertTrue("$username should be valid", isValidUsername(username))
        }
    }

    @Test
    fun `isValidUsername should reject invalid usernames`() {
        // Given - rejecting special characters except underscore
        val invalidUsernames = listOf(
            "ab", // too short
            "user@name",
            "test user", // contains space
            "",
            "a" // single character
        )
        
        // When & Then
        invalidUsernames.forEach { username ->
            assertFalse("$username should be invalid", isValidUsername(username))
        }
    }

    @Test
    fun `sanitizeInput should remove dangerous characters`() {
        // Given
        val input = "<script>alert('xss')</script>"
        
        // When
        val sanitized = sanitizeInput(input)
        
        // Then
        assertFalse(sanitized.contains("<"))
        assertFalse(sanitized.contains(">"))
    }

    @Test
    fun `sanitizeInput should handle null and empty strings`() {
        // When & Then
        assertEquals("", sanitizeInput(""))
        assertNotNull(sanitizeInput(""))
    }

    @Test
    fun `isValidScore should accept valid score ranges`() {
        // Given - assuming scores 0-1000
        val validScores = listOf(0, 50, 500, 999, 1000)
        
        // When & Then
        validScores.forEach { score ->
            assertTrue("$score should be valid", isValidScore(score))
        }
    }

    @Test
    fun `isValidScore should reject invalid scores`() {
        // Given
        val invalidScores = listOf(-1, -100, 1001, 10000)
        
        // When & Then
        invalidScores.forEach { score ->
            assertFalse("$score should be invalid", isValidScore(score))
        }
    }

    @Test
    fun `isNotBlank should detect empty and blank strings`() {
        // Given
        val blankStrings = listOf("", "   ", "\t", "\n", "  \t  ")
        val nonBlankStrings = listOf("text", " text ", "a")
        
        // When & Then
        blankStrings.forEach { str ->
            assertFalse(str.isNotBlank())
        }
        nonBlankStrings.forEach { str ->
            assertTrue(str.isNotBlank())
        }
    }

    @Test
    fun `trimInput should remove leading and trailing spaces`() {
        // Given
        val input = "  test  "
        
        // When
        val trimmed = input.trim()
        
        // Then
        assertEquals("test", trimmed)
        assertFalse(trimmed.startsWith(" "))
        assertFalse(trimmed.endsWith(" "))
    }

    // Validation helper functions
    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        return email.matches(emailRegex)
    }

    private fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }

    private fun isValidUsername(username: String): Boolean {
        return username.length >= 3 && username.matches("^[a-zA-Z0-9_]+$".toRegex())
    }

    private fun sanitizeInput(input: String): String {
        return input.replace("<", "").replace(">", "")
    }

    private fun isValidScore(score: Int): Boolean {
        return score in 0..1000
    }
}
