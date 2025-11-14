package com.mytheclipse.quizbattle.data.repository

import com.mytheclipse.quizbattle.data.remote.websocket.WebSocketManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

class DailyMissionRepository {
    
    private val webSocketManager = WebSocketManager.getInstance()
    
    fun requestDailyMissions(userId: String) {
        val message = mapOf(
            "type" to "daily.mission.list.sync",
            "payload" to mapOf("userId" to userId)
        )
        webSocketManager.sendMessage(message)
    }
    
    fun claimDailyMission(userId: String, missionId: String) {
        val message = mapOf(
            "type" to "daily.mission.claim",
            "payload" to mapOf(
                "userId" to userId,
                "missionId" to missionId
            )
        )
        webSocketManager.sendMessage(message)
    }
    
    fun requestAchievementList(userId: String, unlockedOnly: Boolean = false) {
        val message = mapOf(
            "type" to "achievement.list.sync",
            "payload" to mapOf(
                "userId" to userId,
                "unlockedOnly" to unlockedOnly
            )
        )
        webSocketManager.sendMessage(message)
    }
    
    fun claimAchievement(userId: String, achievementId: String) {
        val message = mapOf(
            "type" to "achievement.claim",
            "payload" to mapOf(
                "userId" to userId,
                "achievementId" to achievementId
            )
        )
        webSocketManager.sendMessage(message)
    }
    
    fun observeDailyMissionEvents(): Flow<DailyMissionEvent> {
        return webSocketManager.messages
            .filter { message ->
                val type = message["type"] as? String
                type?.startsWith("daily.mission.") == true || type?.startsWith("achievement.") == true
            }
            .map { message ->
                parseDailyMissionEvent(message)
            }
    }
    
    @Suppress("UNCHECKED_CAST")
    private fun parseDailyMissionEvent(message: Map<String, Any>): DailyMissionEvent {
        val type = message["type"] as? String ?: ""
        val payload = message["payload"] as? Map<String, Any> ?: emptyMap()
        
        return when (type) {
            "daily.mission.list.data" -> {
                val missionsList = payload["missions"] as? List<Map<String, Any>> ?: emptyList()
                val missions = missionsList.map { m ->
                    val requirement = m["requirement"] as? Map<String, Any> ?: emptyMap()
                    val reward = m["reward"] as? Map<String, Any> ?: emptyMap()
                    
                    DailyMissionInfo(
                        missionId = m["missionId"] as? String ?: "",
                        name = m["name"] as? String ?: "",
                        description = m["description"] as? String ?: "",
                        type = m["type"] as? String ?: "",
                        target = (requirement["target"] as? Double)?.toInt() ?: 0,
                        rewardCoins = (reward["coins"] as? Double)?.toInt() ?: 0,
                        rewardExperience = (reward["experience"] as? Double)?.toInt() ?: 0,
                        progress = (m["progress"] as? Double)?.toInt() ?: 0,
                        isCompleted = m["isCompleted"] as? Boolean ?: false,
                        isClaimed = m["isClaimed"] as? Boolean ?: false,
                        expiresAt = (m["expiresAt"] as? Double)?.toLong() ?: 0L
                    )
                }
                DailyMissionEvent.MissionListData(
                    missions = missions,
                    completedToday = (payload["completedToday"] as? Double)?.toInt() ?: 0,
                    totalMissions = (payload["totalMissions"] as? Double)?.toInt() ?: 0
                )
            }
            "daily.mission.claimed" -> {
                val rewards = payload["rewards"] as? Map<String, Any> ?: emptyMap()
                val newStats = payload["newStats"] as? Map<String, Any> ?: emptyMap()
                
                DailyMissionEvent.MissionClaimed(
                    missionId = payload["missionId"] as? String ?: "",
                    rewardCoins = (rewards["coins"] as? Double)?.toInt() ?: 0,
                    rewardExperience = (rewards["experience"] as? Double)?.toInt() ?: 0,
                    newCoins = (newStats["coins"] as? Double)?.toInt() ?: 0,
                    newExperience = (newStats["experience"] as? Double)?.toInt() ?: 0,
                    newLevel = (newStats["level"] as? Double)?.toInt() ?: 0
                )
            }
            "achievement.list.data" -> {
                val achievementsList = payload["achievements"] as? List<Map<String, Any>> ?: emptyList()
                val achievements = achievementsList.map { a ->
                    AchievementInfo(
                        achievementId = a["achievementId"] as? String ?: "",
                        name = a["name"] as? String ?: "",
                        description = a["description"] as? String ?: "",
                        rarity = a["rarity"] as? String ?: "common",
                        rewardPoints = (a["rewardPoints"] as? Double)?.toInt() ?: 0,
                        rewardCoins = (a["rewardCoins"] as? Double)?.toInt() ?: 0,
                        isUnlocked = a["isUnlocked"] as? Boolean ?: false,
                        unlockedAt = (a["unlockedAt"] as? Double)?.toLong()
                    )
                }
                DailyMissionEvent.AchievementListData(
                    achievements = achievements,
                    totalAchievements = (payload["totalAchievements"] as? Double)?.toInt() ?: 0,
                    unlockedCount = (payload["unlockedCount"] as? Double)?.toInt() ?: 0
                )
            }
            "achievement.unlocked" -> {
                DailyMissionEvent.AchievementUnlocked(
                    achievementId = payload["achievementId"] as? String ?: "",
                    name = payload["name"] as? String ?: "",
                    description = payload["description"] as? String ?: "",
                    rarity = payload["rarity"] as? String ?: "common",
                    rewardPoints = (payload["rewardPoints"] as? Double)?.toInt() ?: 0,
                    rewardCoins = (payload["rewardCoins"] as? Double)?.toInt() ?: 0
                )
            }
            else -> DailyMissionEvent.Unknown
        }
    }
}

data class DailyMissionInfo(
    val missionId: String,
    val name: String,
    val description: String,
    val type: String,
    val target: Int,
    val rewardCoins: Int,
    val rewardExperience: Int,
    val progress: Int,
    val isCompleted: Boolean,
    val isClaimed: Boolean,
    val expiresAt: Long
)

data class AchievementInfo(
    val achievementId: String,
    val name: String,
    val description: String,
    val rarity: String,
    val rewardPoints: Int,
    val rewardCoins: Int,
    val isUnlocked: Boolean,
    val unlockedAt: Long?
)

sealed class DailyMissionEvent {
    data class MissionListData(
        val missions: List<DailyMissionInfo>,
        val completedToday: Int,
        val totalMissions: Int
    ) : DailyMissionEvent()
    
    data class MissionClaimed(
        val missionId: String,
        val rewardCoins: Int,
        val rewardExperience: Int,
        val newCoins: Int,
        val newExperience: Int,
        val newLevel: Int
    ) : DailyMissionEvent()
    
    data class AchievementListData(
        val achievements: List<AchievementInfo>,
        val totalAchievements: Int,
        val unlockedCount: Int
    ) : DailyMissionEvent()
    
    data class AchievementUnlocked(
        val achievementId: String,
        val name: String,
        val description: String,
        val rarity: String,
        val rewardPoints: Int,
        val rewardCoins: Int
    ) : DailyMissionEvent()
    
    object Unknown : DailyMissionEvent()
}
