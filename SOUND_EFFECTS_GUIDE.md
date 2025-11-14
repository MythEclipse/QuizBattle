# Sound Effects Guide

This file documents the sound system for QuizBattle.

## Sound Files Location

Sound files should be placed in: `app/src/main/res/raw/`

## Required Sound Files (when ready to implement)

### Basic Interactions
- `button_click.ogg` - General button click sound
- `button_hover.ogg` - Button hover/focus sound (optional)

### Quiz/Battle Sounds
- `correct_answer.ogg` - Correct answer feedback
- `wrong_answer.ogg` - Wrong answer feedback
- `victory.ogg` - Battle victory sound
- `defeat.ogg` - Battle defeat sound
- `time_warning.ogg` - Timer warning (last 5 seconds)

### Online Features
- `match_found.ogg` - Match found notification
- `message_sent.ogg` - Chat message sent
- `message_received.ogg` - Chat message received
- `notification.ogg` - General notification sound

### Achievements
- `level_up.ogg` - Level up/achievement unlocked
- `mission_complete.ogg` - Mission completed

## File Format Requirements

- **Format**: OGG Vorbis (better compression) or MP3
- **Sample Rate**: 44.1 kHz or 22.05 kHz
- **Bit Rate**: 128 kbps for music, 64 kbps for SFX
- **Duration**: 0.1 - 2.0 seconds for most SFX
- **Volume**: Normalized to -3dB to prevent clipping
- **Naming**: All lowercase, no spaces, use underscore

## Implementation Status

✅ SoundManager utility created
✅ Settings integration (enable/disable toggle)
✅ DataStore persistence for settings
✅ Sound helper functions (SoundUtils)
✅ Button component integration ready
⏳ Actual audio files (to be added)

## Usage Examples

### In Composables
```kotlin
val soundManager = rememberSoundManager()

// Play specific sound
soundManager.playButtonClick()
soundManager.playCorrectAnswer()
soundManager.playVictory()

// Or use helper functions
soundManager.playSound(SoundEffect.BUTTON_CLICK)
```

### In Non-Composable Context
```kotlin
context.playSound(SoundEffect.NOTIFICATION)
```

### With Button Components
```kotlin
QuizBattleButton(
    text = "Play",
    onClick = { /* action */ },
    playSound = true  // Enables sound when audio files available
)
```

## Next Steps

1. Source or create sound effects
2. Add OGG/MP3 files to `app/src/main/res/raw/` folder
3. Uncomment sound loading code in `SoundManager.kt` (line ~40)
4. Test all sounds on physical devices
5. Balance volume levels across all sounds

## Sound Sources

Free sound libraries:
- Freesound.org (CC licensed)
- Zapsplat.com (free tier)
- Pixabay Sounds (free)
- OpenGameArt.org (open source)

Ensure proper licensing for commercial use.
