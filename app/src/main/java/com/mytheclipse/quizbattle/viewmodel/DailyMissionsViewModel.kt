package com.mytheclipse.quizbattle.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mytheclipse.quizbattle.data.repository.DataModels.Mission
import com.mytheclipse.quizbattle.data.repository.DataModels.Achievement
import com.mytheclipse.quizbattle.data.repository.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DailyMissionsState(
    val missions: List<Mission> = emptyList(),
    val achievements: List<Achievement> = emptyList(),
    val completedToday: Int = 0,
    val totalMissions: Int = 0,
    val totalAchievements: Int = 0,
    val unlockedCount: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)

class DailyMissionsViewModel(application: Application) : AndroidViewModel(application) {
    
    private val tokenRepository = TokenRepository(application)
    private val missionRepository = DailyMissionRepository()
    
    private val _state = MutableStateFlow(DailyMissionsState())
    val state: StateFlow<DailyMissionsState> = _state.asStateFlow()
    
    init {
        loadMissions()
        loadAchievements()
        observeMissionEvents()
    }
    
    private fun observeMissionEvents() {
        viewModelScope.launch {
            missionRepository.observeDailyMissionEvents().collect { event ->
                when (event) {
                    is DailyMissionEvent.MissionListData -> {
                        val missions = event.missions.map { m ->
                            DataModels.Mission(
                                missionId = m.missionId,
                                title = m.name,
                                description = m.description,
                                currentProgress = m.progress,
                                targetProgress = m.target,
                                reward = m.rewardCoins,
                                isCompleted = m.isCompleted,
                                isClaimed = m.isClaimed
                            )
                        }
                        _state.value = _state.value.copy(
                            missions = missions,
                            completedToday = event.completedToday,
                            totalMissions = event.totalMissions,
                            isLoading = false
                        )
                    }
                    is DailyMissionEvent.MissionClaimed -> {
                        // Update mission as claimed
                        _state.value = _state.value.copy(
                            missions = _state.value.missions.map { mission ->
                                if (mission.missionId == event.missionId) {
                                    mission.copy(isClaimed = true)
                                } else mission
                            }
                        )
                    }
                    is DailyMissionEvent.AchievementListData -> {
                        val achievements = event.achievements.map { a ->
                            DataModels.Achievement(
                                achievementId = a.achievementId,
                                title = a.name,
                                description = a.description,
                                isUnlocked = a.isUnlocked,
                                unlockedAt = a.unlockedAt
                            )
                        }
                        _state.value = _state.value.copy(
                            achievements = achievements,
                            totalAchievements = event.totalAchievements,
                            unlockedCount = event.unlockedCount,
                            isLoading = false
                        )
                    }
                    is DailyMissionEvent.AchievementUnlocked -> {
                        // Show achievement unlocked notification
                    }
                    else -> {}
                }
            }
        }
    }
    
    fun loadMissions() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val userId = tokenRepository.getUserId() ?: return@launch
            missionRepository.requestDailyMissions(userId)
        }
    }
    
    fun loadAchievements(unlockedOnly: Boolean = false) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val userId = tokenRepository.getUserId() ?: return@launch
            missionRepository.requestAchievementList(userId, unlockedOnly)
        }
    }
    
    fun claimMission(missionId: String) {
        viewModelScope.launch {
            val userId = tokenRepository.getUserId() ?: return@launch
            missionRepository.claimDailyMission(userId, missionId)
        }
    }
    
    fun claimAchievement(achievementId: String) {
        viewModelScope.launch {
            val userId = tokenRepository.getUserId() ?: return@launch
            missionRepository.claimAchievement(userId, achievementId)
        }
    }
}
