package com.mytheclipse.quizbattle.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.offlineActionDataStore: DataStore<Preferences> by preferencesDataStore(name = "offline_actions")

/**
 * Manages offline action queue for network operations
 * Stores actions when offline and executes them when online
 */
class OfflineActionQueue(private val context: Context) {
    
    private val gson = Gson()
    
    companion object {
        private val ACTIONS_KEY = stringPreferencesKey("queued_actions")
        private const val MAX_QUEUE_SIZE = 100
    }
    
    /**
     * Represents an offline action
     */
    data class OfflineAction(
        val id: String,
        val type: ActionType,
        val payload: Map<String, Any>,
        val timestamp: Long = System.currentTimeMillis(),
        val retryCount: Int = 0
    )
    
    enum class ActionType {
        SEND_MESSAGE,
        UPDATE_PROFILE,
        CREATE_POST,
        LIKE_POST,
        ADD_COMMENT,
        SEND_FRIEND_REQUEST,
        ACCEPT_FRIEND_REQUEST,
        SUBMIT_ANSWER,
        UPDATE_SETTINGS
    }
    
    /**
     * Add action to offline queue
     */
    suspend fun queueAction(type: ActionType, payload: Map<String, Any>): String {
        val actionId = generateActionId()
        val action = OfflineAction(
            id = actionId,
            type = type,
            payload = payload
        )
        
        val currentActions = getActions().toMutableList()
        
        // Add new action
        currentActions.add(action)
        
        // Maintain max queue size (remove oldest if exceeded)
        while (currentActions.size > MAX_QUEUE_SIZE) {
            currentActions.removeAt(0)
        }
        
        // Save to DataStore
        saveActions(currentActions)
        
        return actionId
    }
    
    /**
     * Get all queued actions
     */
    suspend fun getActions(): List<OfflineAction> {
        return context.offlineActionDataStore.data.map { preferences ->
            val json = preferences[ACTIONS_KEY] ?: "[]"
            try {
                val type = object : TypeToken<List<OfflineAction>>() {}.type
                gson.fromJson<List<OfflineAction>>(json, type) ?: emptyList()
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }.first()
    }
    
    /**
     * Get actions by type
     */
    suspend fun getActionsByType(type: ActionType): List<OfflineAction> {
        return getActions().filter { it.type == type }
    }
    
    /**
     * Remove action from queue
     */
    suspend fun removeAction(actionId: String) {
        val currentActions = getActions().toMutableList()
        currentActions.removeAll { it.id == actionId }
        saveActions(currentActions)
    }
    
    /**
     * Update action retry count
     */
    suspend fun incrementRetryCount(actionId: String) {
        val currentActions = getActions().toMutableList()
        val index = currentActions.indexOfFirst { it.id == actionId }
        
        if (index != -1) {
            val action = currentActions[index]
            currentActions[index] = action.copy(retryCount = action.retryCount + 1)
            saveActions(currentActions)
        }
    }
    
    /**
     * Clear all actions
     */
    suspend fun clearAll() {
        context.offlineActionDataStore.edit { preferences ->
            preferences.clear()
        }
    }
    
    /**
     * Clear actions older than specified time (in milliseconds)
     */
    suspend fun clearOldActions(maxAge: Long = 7 * 24 * 60 * 60 * 1000L) { // 7 days default
        val currentTime = System.currentTimeMillis()
        val currentActions = getActions().toMutableList()
        
        currentActions.removeAll { action ->
            (currentTime - action.timestamp) > maxAge
        }
        
        saveActions(currentActions)
    }
    
    /**
     * Get queue size
     */
    suspend fun getQueueSize(): Int {
        return getActions().size
    }
    
    /**
     * Check if queue is empty
     */
    suspend fun isEmpty(): Boolean {
        return getActions().isEmpty()
    }
    
    /**
     * Get actions that need retry (failed actions)
     */
    suspend fun getRetryableActions(maxRetries: Int = 3): List<OfflineAction> {
        return getActions().filter { it.retryCount < maxRetries }
    }
    
    private suspend fun saveActions(actions: List<OfflineAction>) {
        context.offlineActionDataStore.edit { preferences ->
            val json = gson.toJson(actions)
            preferences[ACTIONS_KEY] = json
        }
    }
    
    private fun generateActionId(): String {
        return "action_${System.currentTimeMillis()}_${(0..9999).random()}"
    }
}

/**
 * Processor for executing offline actions
 */
class OfflineActionProcessor(
    private val offlineQueue: OfflineActionQueue
) {
    
    /**
     * Process all queued actions
     * Returns list of successfully processed action IDs
     */
    suspend fun processQueue(
        onAction: suspend (OfflineActionQueue.OfflineAction) -> Result<Unit>
    ): ProcessResult {
        val actions = offlineQueue.getRetryableActions()
        val successfulIds = mutableListOf<String>()
        val failedIds = mutableListOf<String>()
        
        actions.forEach { action ->
            try {
                val result = onAction(action)
                
                if (result.isSuccess) {
                    offlineQueue.removeAction(action.id)
                    successfulIds.add(action.id)
                } else {
                    offlineQueue.incrementRetryCount(action.id)
                    failedIds.add(action.id)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                offlineQueue.incrementRetryCount(action.id)
                failedIds.add(action.id)
            }
        }
        
        return ProcessResult(
            totalProcessed = actions.size,
            successful = successfulIds.size,
            failed = failedIds.size,
            successfulIds = successfulIds,
            failedIds = failedIds
        )
    }
    
    /**
     * Process actions of specific type
     */
    suspend fun processActionType(
        type: OfflineActionQueue.ActionType,
        onAction: suspend (OfflineActionQueue.OfflineAction) -> Result<Unit>
    ): ProcessResult {
        val actions = offlineQueue.getActionsByType(type).filter { it.retryCount < 3 }
        val successfulIds = mutableListOf<String>()
        val failedIds = mutableListOf<String>()
        
        actions.forEach { action ->
            try {
                val result = onAction(action)
                
                if (result.isSuccess) {
                    offlineQueue.removeAction(action.id)
                    successfulIds.add(action.id)
                } else {
                    offlineQueue.incrementRetryCount(action.id)
                    failedIds.add(action.id)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                offlineQueue.incrementRetryCount(action.id)
                failedIds.add(action.id)
            }
        }
        
        return ProcessResult(
            totalProcessed = actions.size,
            successful = successfulIds.size,
            failed = failedIds.size,
            successfulIds = successfulIds,
            failedIds = failedIds
        )
    }
    
    data class ProcessResult(
        val totalProcessed: Int,
        val successful: Int,
        val failed: Int,
        val successfulIds: List<String>,
        val failedIds: List<String>
    )
}
