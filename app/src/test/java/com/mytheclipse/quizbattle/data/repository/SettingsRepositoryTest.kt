package com.mytheclipse.quizbattle.data.repository

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for SettingsRepository settings data handling
 */
class SettingsRepositoryTest {

    @Test
    fun `AppSettings should have default values`() {
        // Given & When
        val settings = AppSettings()
        
        // Then
        assertTrue(settings.soundEffectsEnabled)
        assertTrue(settings.musicEnabled)
        assertTrue(settings.vibrationEnabled)
        assertTrue(settings.notificationsEnabled)
    }
    
    @Test
    fun `AppSettings copy should work correctly`() {
        // Given
        val original = AppSettings(
            soundEffectsEnabled = true,
            musicEnabled = true,
            vibrationEnabled = true,
            notificationsEnabled = true
        )
        
        // When
        val modified = original.copy(soundEffectsEnabled = false)
        
        // Then
        assertFalse(modified.soundEffectsEnabled)
        assertTrue(modified.musicEnabled)
        assertTrue(modified.vibrationEnabled)
        assertTrue(modified.notificationsEnabled)
        
        // Original should remain unchanged
        assertTrue(original.soundEffectsEnabled)
    }
    
    @Test
    fun `AppSettings equality should work correctly`() {
        // Given
        val settings1 = AppSettings(true, true, true, true)
        val settings2 = AppSettings(true, true, true, true)
        val settings3 = AppSettings(false, true, true, true)
        
        // Then
        assertEquals(settings1, settings2)
        assertNotEquals(settings1, settings3)
    }
}
