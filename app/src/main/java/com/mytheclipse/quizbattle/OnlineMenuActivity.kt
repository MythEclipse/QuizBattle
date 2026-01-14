package com.mytheclipse.quizbattle

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import com.mytheclipse.quizbattle.databinding.ActivityOnlineMenuBinding
import com.mytheclipse.quizbattle.ui.MatchConfirmationDialog
import com.mytheclipse.quizbattle.viewmodel.ConfirmRequestData
import com.mytheclipse.quizbattle.viewmodel.MatchmakingState
import com.mytheclipse.quizbattle.viewmodel.MatchmakingViewModel

/**
 * Activity for online match menu options.
 * 
 * Provides quick match, create room, join room, and friend battle functionality.
 * Uses [MatchmakingViewModel] for matchmaking state management.
 */
class OnlineMenuActivity : BaseActivity() {

    // region Properties
    
    private lateinit var binding: ActivityOnlineMenuBinding
    private val matchmakingViewModel: MatchmakingViewModel by viewModels()
    
    private var isSearchingForMatch = false
    private var hasNavigated = false
    private var searchStartTime: Long = 0
    
    private val handler = Handler(Looper.getMainLooper())
    private var timerRunnable: Runnable? = null
    private var confirmationDialog: MatchConfirmationDialog? = null
    
    // endregion

    // region Lifecycle
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnlineMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applySystemBarPadding(binding.root)
        
        initializeMatchmaking()
        setupClickListeners()
        observeMatchmakingState()
    }
    
    override fun onResume() {
        super.onResume()
        resetNavigationGuard()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        cleanupResources()
    }
    
    // endregion

    // region Setup
    
    private fun initializeMatchmaking() {
        matchmakingViewModel.connectWebSocket()
        matchmakingViewModel.clearMatchFound()
        matchmakingViewModel.clearConfirmRequest()
        hasNavigated = false
    }
    
    private fun resetNavigationGuard() {
        matchmakingViewModel.clearMatchFound()
        hasNavigated = false
    }
    
    private fun setupClickListeners() {
        binding.backButton?.setOnClickListener { handleBackPress() }
        
        binding.quickMatchButton?.setOnClickListener {
            withDebounce { toggleQuickMatch() }
        }
        
        binding.createRoomButton?.setOnClickListener {
            withDebounce { showCreateRoomDialog() }
        }
        
        binding.joinRoomButton?.setOnClickListener {
            withDebounce { showJoinRoomDialog() }
        }
        
        binding.friendBattleButton?.setOnClickListener {
            withDebounce { navigateTo<FriendListActivity>() }
        }
    }
    
    // endregion

    // region State Observation
    
    private fun observeMatchmakingState() {
        collectState(matchmakingViewModel.state) { state ->
            handleMatchmakingState(state)
        }
    }
    
    private fun handleMatchmakingState(state: MatchmakingState) {
        isSearchingForMatch = state.isSearching
        updateSearchingUI(state.isSearching)
        handleSearchTimer(state)
        handleConfirmRequest(state)
        handleConfirmStatus(state)
        handleMatchFound(state)
        handleError(state)
    }
    
    private fun handleSearchTimer(state: MatchmakingState) {
        if (state.isSearching && timerRunnable == null) {
            state.searchStartTime?.let { startSearchTimer(it) }
        } else if (!state.isSearching) {
            stopSearchTimer()
        }
    }
    
    private fun handleConfirmRequest(state: MatchmakingState) {
        state.confirmRequest?.let { confirmData ->
            showMatchConfirmationDialog(confirmData)
        }
    }
    
    private fun handleConfirmStatus(state: MatchmakingState) {
        state.confirmStatus?.let { statusData ->
            confirmationDialog?.updateWaitingStatus(
                statusData.status,
                statusData.confirmedCount,
                statusData.totalPlayers
            )
        }
    }
    
    private fun handleMatchFound(state: MatchmakingState) {
        state.matchFound?.let { matchData ->
            if (hasNavigated) return
            hasNavigated = true
            
            dismissConfirmationDialog()
            stopSearchTimer()
            
            navigateToOnlineBattle(
                matchId = matchData.matchId,
                opponentName = matchData.opponentName,
                opponentLevel = matchData.opponentLevel,
                category = matchData.category,
                difficulty = matchData.difficulty
            )
        }
    }
    
    private fun handleError(state: MatchmakingState) {
        state.error?.let { error ->
            showToast(error)
            matchmakingViewModel.clearError()
        }
    }
    
    // endregion

    // region Matchmaking Actions
    
    private fun handleBackPress() {
        if (isSearchingForMatch) {
            matchmakingViewModel.cancelMatchmaking()
            stopSearchTimer()
        }
        finish()
    }
    
    private fun toggleQuickMatch() {
        if (isSearchingForMatch) {
            cancelMatchmaking()
        } else {
            startMatchmaking()
        }
    }
    
    private fun startMatchmaking() {
        matchmakingViewModel.findMatch(
            difficulty = DEFAULT_DIFFICULTY,
            category = DEFAULT_CATEGORY
        )
        startSearchTimer()
        showToast(getString(R.string.searching_opponent))
    }
    
    private fun cancelMatchmaking() {
        matchmakingViewModel.cancelMatchmaking()
        stopSearchTimer()
    }
    
    // endregion

    // region Timer Management
    
    private fun startSearchTimer(startTime: Long? = null) {
        searchStartTime = startTime ?: System.currentTimeMillis()
        stopSearchTimer()
        
        timerRunnable = createTimerRunnable()
        handler.post(timerRunnable!!)
    }
    
    private fun createTimerRunnable(): Runnable = object : Runnable {
        override fun run() {
            val elapsedSeconds = ((System.currentTimeMillis() - searchStartTime) / 1000).toInt()
            binding.quickMatchButton?.text = formatSearchTime(elapsedSeconds)
            handler.postDelayed(this, TIMER_UPDATE_INTERVAL)
        }
    }
    
    private fun formatSearchTime(elapsedSeconds: Int): String {
        val minutes = elapsedSeconds / 60
        val seconds = elapsedSeconds % 60
        
        return if (minutes > 0) {
            String.format(CANCEL_SEARCH_FORMAT_MINUTES, minutes, seconds)
        } else {
            String.format(CANCEL_SEARCH_FORMAT_SECONDS, seconds)
        }
    }
    
    private fun stopSearchTimer() {
        timerRunnable?.let { handler.removeCallbacks(it) }
        timerRunnable = null
    }
    
    // endregion

    // region UI Updates
    
    private fun updateSearchingUI(isSearching: Boolean) {
        binding.quickMatchButton?.apply {
            if (!isSearching) {
                text = getString(R.string.quick_match)
            }
        }
        
        binding.createRoomButton?.isEnabled = !isSearching
        binding.joinRoomButton?.isEnabled = !isSearching
        binding.friendBattleButton?.isEnabled = !isSearching
    }
    
    // endregion

    // region Navigation
    
    private fun navigateToOnlineBattle(
        matchId: String,
        opponentName: String,
        opponentLevel: Int,
        category: String,
        difficulty: String
    ) {
        navigateTo<OnlineBattleActivity> {
            putExtra(OnlineBattleActivity.EXTRA_MATCH_ID, matchId)
            putExtra(OnlineBattleActivity.EXTRA_OPPONENT_NAME, opponentName)
            putExtra(OnlineBattleActivity.EXTRA_OPPONENT_LEVEL, opponentLevel)
            putExtra(OnlineBattleActivity.EXTRA_CATEGORY, category)
            putExtra(OnlineBattleActivity.EXTRA_DIFFICULTY, difficulty)
        }
        
        matchmakingViewModel.clearMatchFound()
        finish()
    }
    
    // endregion

    // region Dialogs
    
    private fun showMatchConfirmationDialog(confirmData: ConfirmRequestData) {
        dismissConfirmationDialog()
        
        confirmationDialog = MatchConfirmationDialog.newInstance(
            matchId = confirmData.matchId,
            opponentName = confirmData.opponentName,
            opponentLevel = confirmData.opponentLevel,
            opponentPoints = confirmData.opponentPoints,
            opponentAvatarUrl = confirmData.opponentAvatar,
            difficulty = confirmData.difficulty,
            category = confirmData.category,
            totalQuestions = confirmData.totalQuestions,
            timeoutSeconds = confirmData.expiresIn / 1000
        ).apply {
            setOnAcceptListener { matchId ->
                matchmakingViewModel.confirmMatch(matchId, true)
            }
            setOnDeclineListener { matchId ->
                matchmakingViewModel.confirmMatch(matchId, false)
                matchmakingViewModel.clearConfirmRequest()
            }
        }
        
        confirmationDialog?.show(supportFragmentManager, MatchConfirmationDialog.TAG)
    }
    
    private fun dismissConfirmationDialog() {
        confirmationDialog?.dismiss()
        confirmationDialog = null
    }
    
    private fun showCreateRoomDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.create_lobby)
            .setMessage(R.string.feature_coming_soon)
            .setPositiveButton(R.string.ok, null)
            .show()
    }
    
    private fun showJoinRoomDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.join_lobby)
            .setMessage(R.string.feature_coming_soon)
            .setPositiveButton(R.string.ok, null)
            .show()
    }
    
    // endregion

    // region Utilities
    
    private fun cleanupResources() {
        stopSearchTimer()
        handler.removeCallbacksAndMessages(null)
        
        if (isFinishing && isSearchingForMatch) {
            matchmakingViewModel.cancelMatchmaking()
        }
    }
    
    // endregion

    companion object {
        private const val DEFAULT_DIFFICULTY = "medium"
        private const val DEFAULT_CATEGORY = "general"
        private const val TIMER_UPDATE_INTERVAL = 1000L
        private const val CANCEL_SEARCH_FORMAT_MINUTES = "Cancel Search (%d:%02d)"
        private const val CANCEL_SEARCH_FORMAT_SECONDS = "Cancel Search (%ds)"
    }
}


