package com.mytheclipse.quizbattle.core.util

/**
 * Application-wide constants
 * Centralized location for magic numbers and strings
 */
object Constants {
    
    // ===== App Settings =====
    object App {
        const val PREF_NAME = "quiz_battle_prefs"
        const val DATABASE_NAME = "quiz_battle_database"
        const val WORKER_TAG = "quiz_battle_worker"
    }
    
    // ===== Game Settings =====
    object Game {
        const val DEFAULT_QUESTION_TIME_SECONDS = 10
        const val DEFAULT_QUESTIONS_PER_GAME = 5
        const val MAX_QUESTIONS_PER_GAME = 20
        const val MIN_QUESTIONS_PER_GAME = 3
        
        const val POINTS_CORRECT_ANSWER = 10
        const val POINTS_SPEED_BONUS = 5
        const val POINTS_STREAK_BONUS = 3
        
        const val DIFFICULTY_EASY = "easy"
        const val DIFFICULTY_MEDIUM = "medium"
        const val DIFFICULTY_HARD = "hard"
        
        const val MATCHMAKING_TIMEOUT_MS = 30_000L
        const val GAME_COUNTDOWN_SECONDS = 3
    }
    
    // ===== Network =====
    object Network {
        const val CONNECT_TIMEOUT_SECONDS = 30L
        const val READ_TIMEOUT_SECONDS = 30L
        const val WRITE_TIMEOUT_SECONDS = 30L
        
        const val MAX_RETRY_COUNT = 3
        const val RETRY_DELAY_MS = 1000L
        
        const val WEBSOCKET_RECONNECT_DELAY_MS = 5000L
        const val WEBSOCKET_PING_INTERVAL_MS = 30_000L
    }
    
    // ===== UI =====
    object UI {
        const val ANIMATION_DURATION_SHORT = 150L
        const val ANIMATION_DURATION_MEDIUM = 300L
        const val ANIMATION_DURATION_LONG = 500L
        
        const val DEBOUNCE_CLICK_MS = 500L
        const val SEARCH_DEBOUNCE_MS = 300L
        
        const val TOAST_DURATION_SHORT = 2000L
        const val TOAST_DURATION_LONG = 3500L
        
        const val LEADERBOARD_PAGE_SIZE = 20
        const val HISTORY_PAGE_SIZE = 20
    }
    
    // ===== Cache =====
    object Cache {
        const val MAX_MEMORY_CACHE_MB = 50
        const val MAX_DISK_CACHE_MB = 100
        const val CACHE_STALE_MS = 5 * 60 * 1000L // 5 minutes
    }
    
    // ===== Date Formats =====
    object DateFormat {
        const val API_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
        const val DISPLAY_DATE = "dd MMM yyyy"
        const val DISPLAY_TIME = "HH:mm"
        const val DISPLAY_DATETIME = "dd MMM yyyy, HH:mm"
    }
    
    // ===== Intent Extras =====
    object IntentExtra {
        const val GAME_ID = "extra_game_id"
        const val MATCH_ID = "extra_match_id"
        const val USER_ID = "extra_user_id"
        const val FRIEND_ID = "extra_friend_id"
        const val DIFFICULTY = "extra_difficulty"
        const val CATEGORY = "extra_category"
        const val IS_RANKED = "extra_is_ranked"
    }
    
    // ===== Request Codes =====
    object RequestCode {
        const val GOOGLE_SIGN_IN = 100
        const val PERMISSION_CAMERA = 101
        const val PERMISSION_STORAGE = 102
        const val PICK_IMAGE = 103
    }
    
    // ===== Notification Channels =====
    object Notification {
        const val CHANNEL_GAME = "channel_game"
        const val CHANNEL_SOCIAL = "channel_social"
        const val CHANNEL_GENERAL = "channel_general"
    }
}
